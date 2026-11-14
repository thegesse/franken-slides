const ACCESS_TOKEN_KEY = 'access_token';

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

export async function apiFetch(url, options = {}) {
    const { auth = true, ...requestOptions } = options;
    const headers = new Headers(requestOptions.headers ?? {});
    const accessToken = auth ? getAccessToken() : null;

    if (accessToken) {
        headers.set('Authorization', `Bearer ${accessToken}`);
    }

    const response = await fetch(url, { ...requestOptions, headers });

    if (auth && accessToken && response.status === 401) {
        clearAuthSession();
        window.location.assign('/');
    }

    return response;
}
