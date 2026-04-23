function Header({ currentView, onNavigate }) {
  return (
    <header className="site-header">
      <div className="container header-row">
        <div className="brand">MOVIE<span>TICKER</span></div>

        <nav className="nav-links">
          <button
            type="button"
            className={currentView === 'home' ? 'nav-btn active' : 'nav-btn'}
            onClick={() => onNavigate('home')}
          >
            Trang Chủ
          </button>
          <button type="button" className="nav-btn" disabled>
            Lịch Chiếu
          </button>
          <button type="button" className="nav-btn" disabled>
            Dịch Vụ
          </button>
          <button type="button" className="nav-btn" disabled>
            Khuyến Mãi
          </button>
        </nav>

        <div>
          {currentView === 'home' ? (
            <button type="button" className="action-btn" onClick={() => onNavigate('login')}>
              Đăng Nhập
            </button>
          ) : (
            <button type="button" className="ghost-btn" onClick={() => onNavigate('home')}>
              Về trang chủ
            </button>
          )}
        </div>
      </div>
    </header>
  )
}

export default Header
