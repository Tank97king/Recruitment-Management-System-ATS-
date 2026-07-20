import React, { useState } from 'react';
import PipelineColumn from './PipelineColumn';

const PIPELINE_COLUMNS = [
  { status: 'APPLIED', title: 'Applied', borderClass: 'border-t-blue-500', bgHeader: 'bg-blue-50' },
  { status: 'REVIEWING', title: 'Reviewing', borderClass: 'border-t-amber-500', bgHeader: 'bg-amber-50' },
  { status: 'INTERVIEW', title: 'Interviewing', borderClass: 'border-t-indigo-500', bgHeader: 'bg-indigo-50' },
  { status: 'OFFER', title: 'Job Offer', borderClass: 'border-t-pink-500', bgHeader: 'bg-pink-50' },
  { status: 'HIRED', title: 'Hired', borderClass: 'border-t-emerald-500', bgHeader: 'bg-emerald-50' },
  { status: 'REJECTED', title: 'Rejected', borderClass: 'border-t-red-500', bgHeader: 'bg-red-50' }
];

const PipelineBoard = ({ pipelineData = {}, onMoveApplication }) => {
  const [activeMobileTab, setActiveMobileTab] = useState('APPLIED');

  // Group applications by status from the backend response structure
  // The backend returns application objects under `PipelineResponse` which contains lists or grouped data
  // Let's examine what the PipelineResponse is.
  // Wait, let's look at PipelineResponse DTO in java code to see its fields.
  return (
    <div className="space-y-6">
      {/* Mobile view Tab Navigation */}
      <div className="block md:hidden border border-gray-200 rounded-2xl bg-white p-2.5 shadow-sm overflow-x-auto whitespace-nowrap scrollbar-none">
        <div className="flex gap-1.5">
          {PIPELINE_COLUMNS.map((col) => {
            const list = pipelineData[col.status] || [];
            const isActive = activeMobileTab === col.status;
            return (
              <button
                key={col.status}
                type="button"
                onClick={() => setActiveMobileTab(col.status)}
                className={`px-4 py-2 text-xs font-black uppercase tracking-wider rounded-xl transition-all border ${
                  isActive 
                    ? 'bg-blue-600 border-blue-600 text-white shadow-sm shadow-blue-100' 
                    : 'bg-white border-transparent text-gray-450 hover:text-gray-700'
                }`}
              >
                {col.title} ({list.length})
              </button>
            );
          })}
        </div>
      </div>

      {/* Desktop & Tablet grid */}
      <div className="hidden md:grid md:grid-cols-3 lg:grid-cols-6 gap-5 overflow-x-auto pb-4 scrollbar-thin">
        {PIPELINE_COLUMNS.map((col) => {
          const list = pipelineData[col.status] || [];
          return (
            <div key={col.status} className="min-w-[220px]">
              <PipelineColumn
                title={col.title}
                status={col.status}
                count={list.length}
                applications={list}
                onMove={onMoveApplication}
                borderClass={col.borderClass}
                bgHeaderClass={col.bgHeader}
              />
            </div>
          );
        })}
      </div>

      {/* Single Active Column View on Mobile */}
      <div className="block md:hidden">
        {PIPELINE_COLUMNS.map((col) => {
          if (activeMobileTab !== col.status) return null;
          const list = pipelineData[col.status] || [];
          return (
            <PipelineColumn
              key={col.status}
              title={col.title}
              status={col.status}
              count={list.length}
              applications={list}
              onMove={onMoveApplication}
              borderClass={col.borderClass}
              bgHeaderClass={col.bgHeader}
            />
          );
        })}
      </div>
    </div>
  );
};

export default PipelineBoard;
export { PIPELINE_COLUMNS };
