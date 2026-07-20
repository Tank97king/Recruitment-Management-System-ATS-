import React from 'react';

const SkillBadge = ({ skill, className = '' }) => {
  return (
    <span className={`inline-flex items-center px-2 py-0.5 bg-blue-50 text-blue-700 border border-blue-100 text-xs font-semibold rounded-md ${className}`}>
      {skill}
    </span>
  );
};

export default SkillBadge;
