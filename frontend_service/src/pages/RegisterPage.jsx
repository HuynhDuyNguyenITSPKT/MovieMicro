import { useState } from 'react'
import { requestRegisterOtp, verifyRegisterOtp } from '../services/authService'

function RegisterPage({ onSwitchLogin, onAuthSuccess }) {
  const [registerForm, setRegisterForm] = useState({
    username: '',
    fullName: '',
    email: '',
    password: '',
    confirmPassword: '',
    dateOfBirth: '',
    phone: '',
  })
  const [otp, setOtp] = useState('')
  const [step, setStep] = useState('register')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  const submitRegister = async (event) => {
    event.preventDefault()
    setError('')
    setSuccess('')

    if (registerForm.password !== registerForm.confirmPassword) {
      setError('Mật khẩu xác nhận không khớp')
      return
    }

    setLoading(true)
    try {
      await requestRegisterOtp({
        email: registerForm.email,
        username: registerForm.username,
        password: registerForm.password,
        fullName: registerForm.fullName,
        phone: registerForm.phone,
        dateOfBirth: registerForm.dateOfBirth || null,
      })
      setStep('verify')
      setSuccess('Đã gửi OTP về email. Vui lòng nhập mã để xác thực.')
    } catch (err) {
      setError(err?.response?.data?.message || 'Không gửi được OTP')
    } finally {
      setLoading(false)
    }
  }

  const submitOtp = async (event) => {
    event.preventDefault()
    setError('')
    setSuccess('')
    setLoading(true)

    try {
      const data = await verifyRegisterOtp({
        email: registerForm.email,
        otp,
      })
      setSuccess('Đăng ký thành công. Đang chuyển tới trang hồ sơ...')
      onAuthSuccess?.(data)
    } catch (err) {
      setError(err?.response?.data?.message || 'Xác thực OTP thất bại')
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="auth-layout">
      <section className="auth-left register-bg">
        <div className="auth-copy">
          <h1>MOVIETICKER</h1>
          <p>Tạo tài khoản để đặt vé nhanh hơn, nhận ưu đãi sớm và quản lý lịch sử giao dịch.</p>
        </div>
      </section>

      <section className="auth-right">
        <div className="auth-card large">
          <h2>Đăng ký tài khoản</h2>
          <p className="muted">Nhập thông tin để nhận mã OTP xác thực email.</p>

          {step === 'register' ? (
            <form className="auth-form two-col" onSubmit={submitRegister}>
              <div>
                <label htmlFor="username">Tên tài khoản</label>
                <input id="username" value={registerForm.username} onChange={(e) => setRegisterForm((prev) => ({ ...prev, username: e.target.value }))} required />
              </div>
              <div>
                <label htmlFor="fullName">Họ tên</label>
                <input id="fullName" value={registerForm.fullName} onChange={(e) => setRegisterForm((prev) => ({ ...prev, fullName: e.target.value }))} />
              </div>

              <div className="full-row">
                <label htmlFor="email">Email</label>
                <input id="email" type="email" value={registerForm.email} onChange={(e) => setRegisterForm((prev) => ({ ...prev, email: e.target.value }))} required />
              </div>

              <div>
                <label htmlFor="password">Mat khau</label>
                <input id="password" type="password" value={registerForm.password} onChange={(e) => setRegisterForm((prev) => ({ ...prev, password: e.target.value }))} required />
              </div>
              <div>
                <label htmlFor="confirmPassword">Xác nhận mật khẩu</label>
                <input id="confirmPassword" type="password" value={registerForm.confirmPassword} onChange={(e) => setRegisterForm((prev) => ({ ...prev, confirmPassword: e.target.value }))} required />
              </div>

              <div>
                <label htmlFor="dateOfBirth">Ngày sinh</label>
                <input id="dateOfBirth" type="date" value={registerForm.dateOfBirth} onChange={(e) => setRegisterForm((prev) => ({ ...prev, dateOfBirth: e.target.value }))} />
              </div>
              <div>
                <label htmlFor="phone">Số điện thoại</label>
                <input id="phone" value={registerForm.phone} onChange={(e) => setRegisterForm((prev) => ({ ...prev, phone: e.target.value }))} />
              </div>

              <button type="submit" className="primary-btn full-row" disabled={loading}>
                {loading ? 'Đang gửi OTP...' : 'Đăng ký và nhận OTP'}
              </button>
            </form>
          ) : (
            <form className="auth-form" onSubmit={submitOtp}>
              <label htmlFor="otp">Mã OTP (6 số)</label>
              <input
                id="otp"
                value={otp}
                onChange={(e) => setOtp(e.target.value.replace(/\D/g, '').slice(0, 6))}
                placeholder="Nhập mã OTP"
                required
              />
              <button type="submit" className="primary-btn" disabled={loading || otp.length !== 6}>
                {loading ? 'Đang xác thực...' : 'Xác thực OTP'}
              </button>
              <button type="button" className="ghost-btn form" onClick={() => setStep('register')}>
                Quay lại sửa thông tin
              </button>
            </form>
          )}

          {error ? <p className="error-text">{error}</p> : null}
          {success ? <p className="success-text">{success}</p> : null}

          <p className="switch-text">
            Đã có tài khoản?{' '}
            <button type="button" className="link-btn" onClick={onSwitchLogin}>
              Đăng nhập ngay
            </button>
          </p>

        </div>
      </section>
    </main>
  )
}

export default RegisterPage
