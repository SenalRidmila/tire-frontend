import React from 'react';
import { useNavigate } from 'react-router-dom';
import './Login.css';

function Login() {
  const navigate = useNavigate();

  const handleLogin = () => {
    navigate('/home');
  };

  return (
    <div
      className="login-page"
      style={{
        backgroundImage: "url('/images/background.jpg')",
        backgroundSize: 'cover',
        backgroundPosition: 'center',
        backgroundRepeat: 'no-repeat',
        backgroundAttachment: 'fixed',
        height: '100vh',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center'
      }}
    >
      <div className="login-card">
        <div className="login-left">
          <h2>LOGIN</h2>
          <div className="form-group">
            <label><i className="fa fa-user"></i> User ID</label>
            <input type="text" placeholder="Userid" />
          </div>
          <div className="form-group">
            <label><i className="fa fa-lock"></i> Password</label>
            <input type="password" placeholder="Password" />
          </div>
          <button className="login-btn" onClick={handleLogin}>Login Now</button>
        </div>

        <div className="login-right">
          <img src="/images/logo.png" alt="SLTMobitel Logo" className="logo" />
          <h3>TIRE MANAGEMENT SYSTEM</h3>
          <img src="/images/tiress.png" alt="Tires" className="tires-image" />
        </div>
      </div>
    </div>
  );
}

export default Login;
