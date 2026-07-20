import React from 'react';

const DashboardCard = ({ name, value, icon: Icon, color = 'blue', className = '' }) => {
  const formattedValue = typeof value === 'number' ? new Intl.NumberFormat('en-US').format(value) : value;

  const colorStyles = {
    blue: 'text-blue-600 bg-blue-50 border-blue-100',
    indigo: 'text-indigo-600 bg-indigo-50 border-indigo-100',
    emerald: 'text-emerald-600 bg-emerald-50 border-emerald-100',
    amber: 'text-amber-600 bg-amber-50 border-amber-100',
    teal: 'text-teal-600 bg-teal-50 border-teal-100',
    purple: 'text-purple-600 bg-purple-50 border-purple-100',
  };

  return (
    <div className={`bg-white border border-gray-200 rounded-2xl p-5 shadow-sm hover:shadow-md transition-all duration-100 flex justify-between items-start ${className}`}>
      <div>
        <p className="text-xs font-bold text-gray-400 uppercase tracking-wider">{name}</p>
        <h3 className="text-3xl font-extrabold text-gray-800 mt-2 tracking-tight">{formattedValue}</h3>
      </div>
      <div className={`p-3 rounded-xl border ${colorStyles[color] || colorStyles.blue}`}>
        {Icon && <Icon className="w-5 h-5" />}
      </div>
    </div>
  );
};

export default DashboardCard;
