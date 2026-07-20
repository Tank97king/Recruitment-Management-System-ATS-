import React from 'react';

const SummaryCard = ({ title, count, colorClass, borderClass }) => {
  return (
    <div className={`bg-white border-l-4 ${borderClass} border border-gray-200 rounded-2xl p-5 shadow-sm hover:shadow-md transition-shadow`}>
      <span className="block text-[10px] font-bold text-gray-400 uppercase tracking-wider">{title}</span>
      <span className={`block text-2xl font-black ${colorClass} mt-1`}>{count}</span>
    </div>
  );
};

export default SummaryCard;
