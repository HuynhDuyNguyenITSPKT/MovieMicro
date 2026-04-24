import { useEffect, useRef, useState } from 'react'
import { loginWithGoogle, loginWithPassword } from '../services/authService'

const GSI_SCRIPT_SRC = 'https://accounts.google.com/gsi/client'
const GSI_INIT_FLAG = '__movieticketGsiInitialized'

function LoginPage({ onSwitchRegister, onAuthSuccess }) {
  const [form, setForm] = useState({ login: '', password: '' })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const googleButtonRef = useRef(null)

  const handleGoogleCredential = async (credential) => {
    if (!credential) {
      setError('Không nhận được Google token')
      return
    }

    setLoading(true)
    setError('')
    try {
      const data = await loginWithGoogle(credential)
      onAuthSuccess?.(data)
    } catch (err) {
      setError(err?.response?.data?.message || 'Đăng nhập Google thất bại')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID || ''
    if (!clientId) {
      return
    }

    const ensureGoogleInitialized = () => {
      if (!window.google || !googleButtonRef.current) {
        return
      }

      if (!window[GSI_INIT_FLAG]) {
        window.google.accounts.id.initialize({
          client_id: clientId,
          callback: async (response) => {
            await handleGoogleCredential(response.credential)
          },
        })
        window[GSI_INIT_FLAG] = true
      }

      googleButtonRef.current.innerHTML = ''
      window.google.accounts.id.renderButton(googleButtonRef.current, {
        theme: 'filled_black',
        size: 'large',
        shape: 'pill',
        width: 320,
        text: 'signin_with',
        locale: 'vi',
      })
    }

    const loadScript = () => {
      const script = document.createElement('script')
      script.src = GSI_SCRIPT_SRC
      script.async = true
      script.defer = true
      script.onload = ensureGoogleInitialized
      document.body.appendChild(script)
      return script
    }

    let scriptEl = document.querySelector(`script[src="${GSI_SCRIPT_SRC}"]`)
    if (!scriptEl) {
      scriptEl = loadScript()
    } else if (window.google) {
      ensureGoogleInitialized()
    } else {
      scriptEl.onload = ensureGoogleInitialized
    }

    return () => {
      if (scriptEl && scriptEl.onload === ensureGoogleInitialized) {
        scriptEl.onload = null
      }
    }
  }, [])

  const submitPassword = async (event) => {
    event.preventDefault()
    setLoading(true)
    setError('')

    try {
      const data = await loginWithPassword(form)
      onAuthSuccess?.(data)
    } catch (err) {
      setError(err?.response?.data?.message || 'Đăng nhập thất bại')
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="auth-layout">
      <section className="auth-left login-bg">
        <div className="auth-copy">
          <h1>MOVIETICKER</h1>
          <p>Khám phá thế giới điện ảnh và đặt vé xem phim nhanh chóng, tiện lợi nhất.</p>
        </div>
      </section>

      <section className="auth-right">
        <div className="auth-card">
          <h2>Đăng nhập</h2>
          <p className="muted">Chào mừng bạn trở lại! Vui lòng đăng nhập để tiếp tục.</p>

          <form onSubmit={submitPassword} className="auth-form">
            <label htmlFor="login">Tên tài khoản hoặc email</label>
            <input
              id="login"
              value={form.login}
              onChange={(e) => setForm((prev) => ({ ...prev, login: e.target.value }))}
              placeholder="admin"
              required
            />

            <label htmlFor="password">Mật khẩu</label>
            <input
              id="password"
              type="password"
              value={form.password}
              onChange={(e) => setForm((prev) => ({ ...prev, password: e.target.value }))}
              placeholder="Nhập mật khẩu"
              required
            />

            <button type="submit" className="primary-btn" disabled={loading}>
              {loading ? 'Đang xử lý...' : 'Đăng nhập'}
            </button>
          </form>

          <div className="divider">hoặc</div>

          {import.meta.env.VITE_GOOGLE_CLIENT_ID ? (
            <div ref={googleButtonRef} className="google-wrap" />
          ) : (
            <p className="muted">Thiếu VITE_GOOGLE_CLIENT_ID nên chưa hiển thị nút Google.</p>
          )}

          {error ? <p className="error-text">{error}</p> : null}

          <p className="switch-text">
            Chưa có tài khoản?{' '}
            <button type="button" className="link-btn" onClick={onSwitchRegister}>
              Đăng ký ngay
            </button>
          </p>

        </div>
      </section>
    </main>
  )
}

export default LoginPage
