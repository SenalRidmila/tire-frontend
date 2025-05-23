import React, { useState } from 'react';
import './ViewProfile.css';
import { Link, useNavigate } from 'react-router-dom';

function ViewProfile() {
  const [showLogoutModal, setShowLogoutModal] = useState(false);
  const navigate = useNavigate();

  const handleLogoutClick = () => {
    setShowLogoutModal(true);
  };

  const confirmLogout = () => {
    setShowLogoutModal(false);
    navigate('/login'); // 👈 Redirect to login page
  };

  const cancelLogout = () => {
    setShowLogoutModal(false);
  };

  return (
    <div className="profile-page">
      <header className="profile-header">
  <Link to="/home" className="back-link">← Back to Home</Link>
  <img src="/images/logo.png" alt="SLT Logo" className="slt-logo" />
  <h1>Employee Profile</h1>

  {/* Logout button in top-right */}
  <button className="logout-button-header" onClick={handleLogoutClick}>🚪 Logout</button>
</header>


      <div className="profile-container">
        <div className="profile-content">
          <img src="/images/team3.jpeg" alt="Employee" className="profile-photo" />
          <div className="employee-details">
            <h2>Chalana Perera</h2>
            <p><strong>Age:</strong> 26</p>
            <p><strong>Email:</strong> chalani.perera@slt.lk</p>
            <p><strong>Phone Number:</strong> +94 71 123 4567</p>
            <p><strong>Address:</strong> No. 45, Galle Road, Colombo 03</p>
            <p><strong>Position:</strong> Software Engineer</p>
            <p><strong>Department:</strong> IT Solutions</p>
            <p><strong>Job Starting Date:</strong> January 10, 2022</p>
          </div>
        </div>
      </div>

      <footer className="footer">
        <div className="contact-info">
          <p>📞 +94 112 021 000</p>
          <p>📧 pr@slt.lk</p>
          <p>🏢 Sri Lanka Telecom PLC<br />Lotus Road, P.O.Box 503, Colombo 01</p>
        </div>
      </footer>
      
      <div className="copyright">
        © 2025 Sri Lanka Telecom. All Rights Reserved.
      </div>

      {/* Logout Modal */}
      {showLogoutModal && (
        <div className="logout-modal">
          <div className="logout-box">
            <p>Are you sure you want to log out?</p>
            <div className="logout-buttons">
              <button className="yes-btn" onClick={confirmLogout}>Yes</button>
              <button className="no-btn" onClick={cancelLogout}>No</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default ViewProfile;
