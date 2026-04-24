function ProfilePage({ user }) {
  return (
    <main className="profile-main container">
      <section className="profile-card">
        <p className="profile-badge">Dang nhap thanh cong</p>
        <h1>Xin chao, {user?.username || 'ban'}!</h1>
        <p className="profile-subtitle">Tai khoan cua ban da san sang dat ve va quan ly giao dich.</p>

        <div className="profile-grid">
          <div className="profile-field">
            <span>Account ID</span>
            <strong>{user?.accountId ?? '-'}</strong>
          </div>

          <div className="profile-field">
            <span>Role</span>
            <strong>{user?.role || '-'}</strong>
          </div>

          <div className="profile-field full-width">
            <span>Email</span>
            <strong>{user?.email || '-'}</strong>
          </div>
        </div>
      </section>
    </main>
  )
}

export default ProfilePage
