import React from 'react';

const AuthCard = ({ children, className = '' }) => {
  return (
    <div className={`bg-white py-8 px-4 shadow-md sm:rounded-2xl sm:px-10 border border-gray-100 ${className}`}>
      {children}
    </div>
  );
};

export default AuthCard;
