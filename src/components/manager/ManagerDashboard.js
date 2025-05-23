import React, { useState, useEffect } from 'react';
import './ManagerDashboard.css';

function ManagerDashboard() {
  const [requests, setRequests] = useState([]);

  useEffect(() => {
    setRequests([
      {
        id: 1,
        user: "Driver A",
        tireSize: "225/45R17",
        imageUrl: "https://via.placeholder.com/300",
        status: "Pending",
      },
      {
        id: 2,
        user: "Driver B",
        tireSize: "195/65R15",
        imageUrl: "https://via.placeholder.com/300",
        status: "Pending",
      },
    ]);
  }, []);

  const handleApprove = (id) => {
    alert(`✅ Manager approved request #${id}. Email sent to TTO.`);
    setRequests((prev) =>
      prev.map((r) =>
        r.id === id ? { ...r, status: "✅ Approved by Manager" } : r
      )
    );
  };

  return (
    <div className="manager-dashboard">
      <div className="manager-hero">
        <div className="overlay"></div>
        <div className="hero-text">
          <h1>🧑‍💼 Manager Dashboard - Tire Request</h1>
          <p>View and approve tire requests submitted by users.</p>
        </div>
      </div>

      <div className="manager-content">
        {requests.map((req) => (
          <div key={req.id} className="card">
            <img src={req.imageUrl} alt="Tire" />
            <h3>{req.user}</h3>
            <p><strong>Tire Size:</strong> {req.tireSize}</p>
            <p className={`status ${req.status.includes("Approved") ? 'approved' : 'pending'}`}>{req.status}</p>
            {!req.status.includes("Approved") && (
              <button onClick={() => handleApprove(req.id)}>Approve Request</button>
            )}
          </div>
        ))}
      </div>

      <div className="manager-footer">&copy; 2025 Tire Management System | Manager Panel</div>
    </div>
  );
}

export default ManagerDashboard;
