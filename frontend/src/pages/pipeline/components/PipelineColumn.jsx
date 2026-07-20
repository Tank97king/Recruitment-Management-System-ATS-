import React from 'react';
import { useTranslation } from 'react-i18next';
import ApplicationCard from './ApplicationCard';

const PipelineColumn = ({ title, status, count, applications, onMove, borderClass, bgHeaderClass }) => {
  const { t } = useTranslation();
  
  const handleDragOver = (e) => {
    e.preventDefault();
  };

  const handleDrop = (e) => {
    e.preventDefault();
    const applicationId = e.dataTransfer.getData('text/plain');
    if (applicationId) {
      onMove(applicationId, status);
    }
  };

  return (
    <div 
      onDragOver={handleDragOver}
      onDrop={handleDrop}
      className="bg-gray-50/50 border border-gray-200 rounded-2xl p-4 flex flex-col min-h-[550px] space-y-4 transition-colors hover:bg-gray-50"
    >
      {/* Column Header */}
      <div className={`flex items-center justify-between p-3 rounded-xl border border-gray-200 bg-white shadow-sm border-t-4 ${borderClass}`}>
        <h3 className="font-extrabold text-xs text-gray-800 uppercase tracking-wider">{title}</h3>
        <span className="px-2 py-0.5 text-[10px] font-black text-gray-500 bg-gray-100 rounded-md border border-gray-200">
          {count}
        </span>
      </div>

      {/* Cards list container */}
      <div className="flex-1 overflow-y-auto space-y-3 pr-1 max-h-[600px] scrollbar-thin">
        {applications.length === 0 ? (
          <div className="h-full flex items-center justify-center py-12 border border-dashed border-gray-200 rounded-xl">
            <span className="text-[10px] font-bold text-gray-400 uppercase tracking-widest">{t('pipeline.noApplicationsInStage', 'No candidates')}</span>
          </div>
        ) : (
          applications.map((app) => (
            <ApplicationCard 
              key={app.id} 
              application={app} 
              onMove={onMove} 
            />
          ))
        )}
      </div>
    </div>
  );
};

export default PipelineColumn;
