import React from 'react';

const SectionTitle = ({ children, description, className = '' }) => {
  return (
    <div className={`space-y-1 ${className}`}>
      <h3 className="text-lg font-bold text-gray-800 tracking-tight">{children}</h3>
      {description && <p className="text-xs font-semibold text-gray-450">{description}</p>}
    </div>
  );
};

export default SectionTitle;
