import React from 'react';

const LoadingCard = ({ rows = 4, className = '' }) => {
  return (
    <div className={`bg-white border border-gray-205/70 rounded-2xl p-6 shadow-sm animate-pulse space-y-4 ${className}`}>
      <div className="h-4 bg-gray-200 rounded-md w-1/3"></div>
      <div className="border-t border-gray-100 pt-4 space-y-3">
        {Array.from({ length: rows }).map((_, idx) => (
          <div key={idx} className="space-y-1.5">
            <div className="flex justify-between">
              <div className="h-3 bg-gray-200 rounded-md w-1/4"></div>
              <div className="h-3 bg-gray-200 rounded-md w-1/12"></div>
            </div>
            <div className="h-2 bg-gray-100 rounded-full w-full"></div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default LoadingCard;
