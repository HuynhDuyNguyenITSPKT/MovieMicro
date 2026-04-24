import { useEffect, useMemo, useState } from 'react'
import Header from './components/Header'
import HomePage from './pages/HomePage'
import LoginPage from './pages/LoginPage'
import ProfilePage from './pages/ProfilePage'
import RegisterPage from './pages/RegisterPage'
import { logout } from './services/authService'
import { AUTH_LOGOUT_EVENT } from './services/apiClient'
import './App.css'

const AUTH_USER_KEY = 'movieticket_auth_user'

const getStoredAuthUser = () => {
  if (typeof window === 'undefined') {
    return null
  }

  const raw = window.localStorage.getItem(AUTH_USER_KEY)
  if (!raw) {
    return null
  }

  try {
    return JSON.parse(raw)
  } catch {
    window.localStorage.removeItem(AUTH_USER_KEY)
    return null
  }
}

const buildAuthUser = (authResponse) => ({
  accountId: authResponse.accountId,
  email: authResponse.email,
  username: authResponse.username,
  role: authResponse.role,
})

function App() {
  const [authUser, setAuthUser] = useState(() => getStoredAuthUser())
  const [view, setView] = useState(() => (getStoredAuthUser() ? 'profile' : 'home'))

  const isAuthenticated = useMemo(() => Boolean(authUser), [authUser])

  const handleAuthSuccess = (authResponse) => {
    const user = buildAuthUser(authResponse)
    setAuthUser(user)

    if (typeof window !== 'undefined') {
      window.localStorage.setItem(AUTH_USER_KEY, JSON.stringify(user))
    }

    setView('profile')
  }

  const handleLogout = () => {
    logout()
    setAuthUser(null)

    if (typeof window !== 'undefined') {
      window.localStorage.removeItem(AUTH_USER_KEY)
    }

    setView('home')
  }

  useEffect(() => {
    if (typeof window === 'undefined') {
      return undefined
    }

    const handleAuthCleared = () => {
      setAuthUser(null)
      window.localStorage.removeItem(AUTH_USER_KEY)
      setView('home')
    }

    window.addEventListener(AUTH_LOGOUT_EVENT, handleAuthCleared)
    return () => {
      window.removeEventListener(AUTH_LOGOUT_EVENT, handleAuthCleared)
    }
  }, [])

  const renderCurrentView = () => {
    if (view === 'login') {
      return <LoginPage onSwitchRegister={() => setView('register')} onAuthSuccess={handleAuthSuccess} />
    }

    if (view === 'register') {
      return <RegisterPage onSwitchLogin={() => setView('login')} onAuthSuccess={handleAuthSuccess} />
    }

    if (view === 'profile' && isAuthenticated) {
      return <ProfilePage user={authUser} />
    }

    return <HomePage />
  }

  return (
    <div className="app-root">
      <Header
        currentView={view}
        onNavigate={setView}
        isAuthenticated={isAuthenticated}
        onLogout={handleLogout}
      />
      {renderCurrentView()}
    </div>
  )
}

export default App
