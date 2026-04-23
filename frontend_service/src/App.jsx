import { useState } from 'react'
import Header from './components/Header'
import HomePage from './pages/HomePage'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import './App.css'

function App() {
  const [view, setView] = useState('home')

  const renderCurrentView = () => {
    if (view === 'login') {
      return <LoginPage onSwitchRegister={() => setView('register')} />
    }

    if (view === 'register') {
      return <RegisterPage onSwitchLogin={() => setView('login')} />
    }

    return <HomePage />
  }

  return (
    <div className="app-root">
      <Header currentView={view} onNavigate={setView} />
      {renderCurrentView()}
    </div>
  )
}

export default App
