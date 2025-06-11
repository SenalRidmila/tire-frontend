import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';

import Login from './components/Login';
import Home from './components/Home';
import RequestForm from './components/RequestForm';
import About from './components/About';
import Contact from './components/Contact';
import ViewProfile from './components/ViewProfile';
<<<<<<< HEAD
import TTODashboard from './components/tto/TTODashboard';
import ManagerDashboard from './components/manager/ManagerDashboard';
=======
>>>>>>> 3439c07 (Crud Table)

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Navigate to="/login" />} /> {/* Redirect to login */}
        <Route path="/login" element={<Login />} />
        <Route path="/home" element={<Home />} />
        <Route path="/request" element={<RequestForm />} />
        <Route path="/about" element={<About />} />
        <Route path="/contact" element={<Contact />} />
        <Route path="/view-profile" element={<ViewProfile />} />
<<<<<<< HEAD
        <Route path="/tto-dashboard" element={<TTODashboard />} />
        <Route path="/manager-dashboard" element={<ManagerDashboard />} />
=======
>>>>>>> 3439c07 (Crud Table)
      </Routes>
    </Router>
  );
}

export default App;
