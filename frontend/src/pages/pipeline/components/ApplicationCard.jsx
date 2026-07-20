import React, { useState } from 'react';
import { Calendar, Briefcase, Building2, ChevronRight, GripVertical } from 'lucide-react';
import { Link } from 'react-router-dom';

const ApplicationCard = ({ application, onMove }) => {
  const [loading, setLoading] = useState(false);

  const getValidTransitions = (status) => {
    switch (status) {
      case 'APPLIED':
        return ['REVIEWING', 'REJECTED'];
      case 'REVIEWING':
        return ['INTERVIEW', 'REJECTED'];
      case 'INTERVIEW':
        return ['OFFER', 'REJECTED'];
      case 'OFFER':
        return ['HIRED', 'REJECTED'];
      default:
        return [];
    }
  };

  const status = application.currentStatus || application.applicationStatus;
  const transitions = getValidTransitions(status);

  const handleSelectTransition = async (e) => {
    const nextStatus = e.target.value;
    if (!nextStatus) return;
    setLoading(true);
    try {
      await onMove(application.id, nextStatus);
    } finally {
      setLoading(false);
    }
  };

  const handleDragStart = (e) => {
    e.dataTransfer.setData('text/plain', application.id);
    e.dataTransfer.effectAllowed = 'move';
  };

  const formatDate = (isoString) => {
    if (!isoString) return 'N/A';
    return new Date(isoString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  return (
    <div
      draggable={transitions.length > 0}
      onDragStart={handleDragStart}
      className={`bg-white border border-gray-200 rounded-xl p-4 shadow-sm hover:shadow-md transition-all group relative ${
        transitions.length > 0 ? 'cursor-grab active:cursor-grabbing' : 'opacity-85'
      }`}
    >
      {/* Top drag handle indicator */}
      {transitions.length > 0 && (
        <div className="absolute left-2 top-1/2 -translate-y-1/2 text-gray-300 hover:text-gray-400 p-0.5 rounded opacity-0 group-hover:opacity-100 transition-opacity">
          <GripVertical className="w-4 h-4" />
        </div>
      )}

      <div className={transitions.length > 0 ? 'pl-3' : ''}>
        {/* Header containing name */}
        <div className="flex items-start justify-between gap-2">
          <Link
            to={`/candidates/${application.candidateId}`}
            className="font-bold text-sm text-gray-800 hover:text-blue-600 transition-colors block"
          >
            {application.candidateName}
          </Link>
          
          <Link
            to={`/applications/${application.id}`}
            className="p-1 rounded-lg text-gray-400 hover:text-blue-500 hover:bg-blue-50 border border-transparent hover:border-blue-100 transition-all opacity-0 group-hover:opacity-100"
            title="View Details"
          >
            <ChevronRight className="w-4 h-4" />
          </Link>
        </div>

        {/* Job opening details */}
        <div className="space-y-1 mt-2 text-xs font-semibold text-gray-500">
          <div className="flex items-center gap-1.5">
            <Briefcase className="w-3.5 h-3.5 text-gray-400 flex-shrink-0" />
            <span className="truncate">{application.jobTitle}</span>
          </div>
          <div className="flex items-center gap-1.5">
            <Building2 className="w-3.5 h-3.5 text-gray-400 flex-shrink-0" />
            <span className="truncate">{application.companyName}</span>
          </div>
          <div className="flex items-center gap-1.5 pt-1 text-[10px] text-gray-400">
            <Calendar className="w-3.5 h-3.5" />
            <span>Applied: {formatDate(application.appliedAt)}</span>
          </div>
        </div>

        {/* Move To Dropdown */}
        {transitions.length > 0 && (
          <div className="mt-4 pt-3 border-t border-gray-100 flex items-center justify-between gap-2">
            <span className="text-[10px] font-bold text-gray-400 uppercase tracking-wider">Move to:</span>
            <select
              onChange={handleSelectTransition}
              disabled={loading}
              value=""
              className="px-2.5 py-1 border border-gray-200 rounded-lg text-[10px] font-bold text-gray-600 bg-gray-50 focus:outline-none focus:ring-1 focus:ring-blue-500 max-w-[120px]"
            >
              <option value="" disabled>Select stage</option>
              {transitions.map((stage) => (
                <option key={stage} value={stage}>
                  {stage}
                </option>
              ))}
            </select>
          </div>
        )}
      </div>
    </div>
  );
};

export default ApplicationCard;
