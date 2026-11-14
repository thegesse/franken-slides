import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import Dashboard from './pages/dashboard/Dashboard.jsx';
import Login from './pages/login.jsx';
import PresentationPage from './pages/PresentationPage.jsx';
import Register from './pages/register.jsx';
import { getAccessToken } from './api.js';
import './App.css';

function RequireUser({ children }) {
    const userId = localStorage.getItem('user_id');
    return userId && getAccessToken() ? children : <Navigate to="/" replace />;
}

function HomeRoute() {
    const userId = localStorage.getItem('user_id');
    return <Navigate to={userId && getAccessToken() ? '/dashboard' : '/'} replace />;
}

export default function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<Login />} />
                <Route path="/signup" element={<Register />} />
                <Route
                    path="/dashboard"
                    element={
                        <RequireUser>
                            <Dashboard />
                        </RequireUser>
                    }
                />
                <Route
                    path="/presentation"
                    element={
                        <RequireUser>
                            <PresentationPage />
                        </RequireUser>
                    }
                />
                <Route path="*" element={<HomeRoute />} />
            </Routes>
        </BrowserRouter>
    );
}
