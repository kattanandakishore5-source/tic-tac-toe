import { lazy, Suspense } from 'react'
import { BrowserRouter, Routes, Route, Navigate, useLocation } from 'react-router-dom'
import NavigationBar from './components/NavigationBar'
import './App.css'

const GamePage = lazy(() => import('./pages/GamePage'))
const LoginPage = lazy(() => import('./pages/LoginPage'))
const RegisterPage = lazy(() => import('./pages/RegisterPage'))
const ProfilePage = lazy(() => import('./pages/ProfilePage'))
const FriendsPage = lazy(() => import('./pages/FriendsPage'))
const GameHistoryPage = lazy(() => import('./pages/GameHistoryPage'))

function AppLayout() {
  const location = useLocation();
  const hideNav = location.pathname === '/login' || location.pathname === '/register';

  return (
    <div className="app">
      {!hideNav && <NavigationBar />}
      <Suspense fallback={<div className="loading-screen">Loading...</div>}>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/game" element={<GamePage />} />
          <Route path="/profile" element={<ProfilePage />} />
          <Route path="/friends" element={<FriendsPage />} />
          <Route path="/history" element={<GameHistoryPage />} />
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </Suspense>
    </div>
  );
}

function App() {
  return (
    <BrowserRouter>
      <AppLayout />
    </BrowserRouter>
  )
}

export default App
