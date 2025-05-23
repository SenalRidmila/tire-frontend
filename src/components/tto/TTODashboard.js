import React, { useState, useEffect } from 'react';
import './TTODashboard.css';

function TTODashboard() {
  const [requests, setRequests] = useState([]);

  useEffect(() => {
    setRequests([
      {
        id: 1,
        user: "Driver A",
        tireSize: "225/45R17",
        imageUrl: "https://via.placeholder.com/300",
        status: "Approved by Manager",
      },
      {
        id: 2,
        user: "Driver B",
        tireSize: "195/65R15",
        imageUrl: "https://via.placeholder.com/300",
        status: "Approved by Manager",
      },
    ]);
  }, []);

  const handleFinalApprove = (id) => {
    alert(`✅ TTO approved request #${id}. Tire can be dispatched.`);
    setRequests((prev) =>
      prev.map((r) =>
        r.id === id ? { ...r, status: "✅ Final Approved by TTO" } : r
      )
    );
  };

  return (
    <div className="tto-dashboard">
      <div className="manager-hero">
        <div className="overlay"></div>
        <div className="hero-text">
          <h1 className="tto-title">🛠️ Transport Officer Dashboard - Tire Requests</h1>
          <p>View and approve tire requests submitted by users.</p>
        </div>
      </div>

      <div className="tto-grid">
        {requests.map((req) => (
          <div key={req.id} className="tto-card">
            <img src={req.imageUrl} alt="Tire" />
            <div>
              <h2>👤 {req.user}</h2>
              <p>Tire Size: {req.tireSize}</p>
              <p className={`tto-status ${req.status.includes("Final") ? "approved" : "pending"}`}>
                {req.status}
              </p>
              {!req.status.includes("Final") && (
                <button
                  onClick={() => handleFinalApprove(req.id)}
                  className="tto-button"
                >
                  Final Approve
                </button>
              )}
            </div>
          </div>
        ))}
      </div>

      {/* Footer */}
      <footer className="about-footer">
        <p>© 2025 SLT Tire Management System. All rights reserved.</p>
      </footer>
    </div>
  );
}

export default TTODashboard;
