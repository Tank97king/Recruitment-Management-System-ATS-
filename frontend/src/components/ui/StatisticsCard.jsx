import React from 'react';

const StatisticsCard = ({ title, entries = [], className = '' }) => {
  const total = entries.reduce((sum, item) => sum + item.value, 0);

  const getPercentage = (value) => {
    if (total === 0) return 0;
    return Math.round((value / total) * 100);
  };

  const formatNum = (value) => {
    return new Intl.NumberFormat('en-US').format(value);
  };

  return (
    <div className={`bg-white border border-gray-200 rounded-2xl p-6 shadow-sm flex flex-col justify-between ${className}`}>
      <div>
        <h4 className="font-bold text-gray-800 text-sm border-b border-gray-100 pb-3 uppercase tracking-wider">{title}</h4>
        
        <div className="mt-5 space-y-4">
          {entries.map((item) => {
            const pct = getPercentage(item.value);
            return (
              <div key={item.label} className="space-y-1.5">
                <div className="flex justify-between text-xs font-semibold">
                  <span className="text-gray-500">{item.label}</span>
                  <span className="text-gray-900 font-bold">
                    {formatNum(item.value)} <span className="text-gray-400 font-normal">({pct}%)</span>
                  </span>
                </div>
                <div className="w-full bg-gray-100 h-2 rounded-full overflow-hidden">
                  <div 
                    className={`h-full ${item.color || 'bg-blue-500'} rounded-full transition-all duration-300`} 
                    style={{ width: `${pct}%` }}
                  />
                </div>
              </div>
            );
          })}
        </div>
      </div>
      
      <div className="mt-6 pt-4 border-t border-gray-100 flex justify-between items-center text-xs font-semibold text-gray-400">
        <span>TOTAL RECORDED</span>
        <span className="text-gray-800 font-extrabold">{formatNum(total)}</span>
      </div>
    </div>
  );
};

export default StatisticsCard;
