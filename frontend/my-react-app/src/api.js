const ACCESS_TOKEN_KEY = 'access_token';
let csrfToken;
let csrfRequest;
let refreshRequest;

export function getAccessToken() {
    return sessionStorage.getItem(ACCESS_TOKEN_KEY);
}

export function saveAuthSession(authResponse, fallbackUsername = '') {
    if (!authResponse?.accessToken) {
        throw new Error('The server response did not include an access token.');
    }

    sessionStorage.setItem(ACCESS_TOKEN_KEY, authResponse.accessToken);

    if (authResponse.id != null) {
        localStorage.setItem('user_id', String(authResponse.id));
    }

    const username = authResponse.username || fallbackUsername;
    if (username) {
        localStorage.setItem('username', username);
    }
}

export function clearAuthSession() {
    sessionStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem('user_id');
    localStorage.removeItem('username');
}

function csrfUrl(requestUrl) {
    return `${new URL(requestUrl, window.location.href).origin}/api/v1/auth/csrf`;
}

async function getCsrfToken(requestUrl) {
    if (csrfToken) return csrfToken;
    if (!csrfRequest) {
        csrfRequest = fetch(csrfUrl(requestUrl), { credentials: 'include' })
            .then(async (response) => {
                if (!response.ok) throw new Error(`Could not get CSRF token (${response.status})`);
                const data = await response.json();
                csrfToken = data.token;
                return csrfToken;
            })
            .finally(() => { csrfRequest = null; });
    }
    return csrfRequest;
}

async function refreshAccessToken(requestUrl) {
    if (!refreshRequest) {
        refreshRequest = (async () => {
            const token = await getCsrfToken(requestUrl);
            const response = await fetch(`${new URL(requestUrl, window.location.href).origin}/api/v1/auth/refresh`, {
                method: 'POST',
                credentials: 'include',
                headers: { 'X-XSRF-TOKEN': token }
            });
            if (!response.ok) throw new Error('Session expired');
            const data = await response.json();
            if (!data.accessToken) throw new Error('Refresh response did not include an access token');
            sessionStorage.setItem(ACCESS_TOKEN_KEY, data.accessToken);
            return data.accessToken;
        })().finally(() => { refreshRequest = null; });
    }
    return refreshRequest;
}

function isMutation(method = 'GET') {
    return !['GET', 'HEAD', 'OPTIONS', 'TRACE'].includes(method.toUpperCase());
}

export async function apiFetch(url, options = {}) {
    const { auth = true, ...requestOptions } = options;
    const method = requestOptions.method ?? 'GET';
    const headers = new Headers(requestOptions.headers ?? {});
    let accessToken = auth ? getAccessToken() : null;

    if (isMutation(method)) {
        headers.set('X-XSRF-TOKEN', await getCsrfToken(url));
    }

    const send = (token) => {
        const requestHeaders = new Headers(headers);
        if (token) requestHeaders.set('Authorization', `Bearer ${token}`);
        else requestHeaders.delete('Authorization');
        return fetch(url, { ...requestOptions, credentials: 'include', headers: requestHeaders });
    };

    // A saved refresh cookie can restore the short lived access token after a page reload.
    if (auth && !accessToken) {
        try {
            accessToken = await refreshAccessToken(url);
        } catch {
            clearAuthSession();
            window.location.assign('/');
            throw new Error('Your session has expired. Please log in again.');
        }
    }

    let response = await send(accessToken);
    if (auth && response.status === 401) {
        try {
            accessToken = await refreshAccessToken(url);
            response = await send(accessToken);
        } catch {
            clearAuthSession();
            window.location.assign('/');
        }
    }
    return response;
}
