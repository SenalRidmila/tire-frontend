import React from 'react';
import { useNavigate } from 'react-router-dom';

function Home() {
  const navigate = useNavigate(); // React Router hook to navigate

  const handleViewProfile = () => {
    // Navigate to the Profile page when the button is clicked
    navigate('/profile');
  };

  return (
    <div className="min-h-screen bg-gray-100 flex items-center justify-center">
      <div className="bg-white w-full max-w-lg p-8 rounded-lg shadow-md">
        <h2 className="text-3xl font-bold mb-6 text-center">Welcome to the Home Page</h2>
        <p className="text-gray-700 text-center mb-4">You have successfully logged in.</p>

        <div className="flex justify-center mb-4">
          <button
            onClick={handleViewProfile} // Navigate to Profile page
            className="bg-indigo-500 text-white px-6 py-2 rounded-md hover:bg-indigo-600"
          >
            View Profile
          </button>
        </div>

        <div className="flex justify-center">
          <button className="bg-indigo-500 text-white px-6 py-2 rounded-md hover:bg-indigo-600">
            Go to Dashboard
          </button>
        </div>
      </div>
    </div>
  );
}

export default Home;
