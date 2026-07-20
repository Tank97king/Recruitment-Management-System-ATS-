import React, { useState, useEffect } from 'react';
import { X, SlidersHorizontal } from 'lucide-react';
import SubmitButton from '../../../components/ui/SubmitButton';

const StatusUpdateModal = ({ open, currentStatus, onConfirm, onCancel, loading }) => {
  const [selectedStatus, setSelectedStatus] = useState('');

  const getTransitionOptions = (status) => {
    switch (status) {
      case 'APPLIED':
        return [
          { value: 'REVIEWING', label: 'Under Review' },
          { value: 'REJECTED', label: 'Rejected' },
        ];
      case 'REVIEWING':
        return [
          { value: 'INTERVIEW', label: 'Interview Scheduled' },
          { value: 'REJECTED', label: 'Rejected' },
        ];
      case 'INTERVIEW':
        return [
          { value: 'OFFER', label: 'Job Offer Extended' },
          { value: 'REJECTED', label: 'Rejected' },
        ];
      case 'OFFER':
        return [
          { value: 'HIRED', label: 'Candidate Hired' },
          { value: 'REJECTED', label: 'Rejected' },
        ];
      default:
        return [];
    }
  };

  const options = getTransitionOptions(currentStatus);

  useEffect(() => {
    if (options.length > 0) {
      setSelectedStatus(options[0].value);
    } else {
      setSelectedStatus('');
    }
  }, [currentStatus]);

  if (!open) return null;

  const handleSubmit = (e) => {
    e.preventDefault();
    if (selectedStatus) {
      onConfirm(selectedStatus);
    }
  };

  const isTerminal = options.length === 0;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/55 animate-fadeIn">
      <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-xl relative border border-gray-100">
        <button 
          onClick={onCancel}
          disabled={loading}
          className="absolute right-4 top-4 p-1 text-gray-400 hover:text-gray-700 rounded-lg hover:bg-gray-50 disabled:opacity-50"
        >
          <X className="w-5 h-5" />
        </button>

        <h3 className="font-bold text-gray-800 text-base mb-1">Update Application Status</h3>
        <p className="text-xs text-gray-400 font-semibold mb-6 uppercase tracking-wider">Advance hiring process workflow</p>

        {isTerminal ? (
          <div className="space-y-4">
            <div className="p-4 bg-gray-50 border border-gray-200 rounded-xl text-xs font-semibold text-gray-500 text-center">
              This application has reached a terminal hiring status ({currentStatus}) and cannot be transitioned further.
            </div>
            <div className="flex justify-end pt-2">
              <button
                type="button"
                onClick={onCancel}
                className="px-4 py-2 border border-gray-200 hover:bg-gray-50 text-gray-500 text-xs font-bold rounded-xl transition-colors"
              >
                Close
              </button>
            </div>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-1.5">
              <label htmlFor="nextStatus" className="block text-sm font-semibold text-gray-700">
                Next hiring stage
              </label>
              <div className="relative rounded-md shadow-sm">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <SlidersHorizontal className="h-5 w-5 text-gray-400" />
                </div>
                <select
                  id="nextStatus"
                  value={selectedStatus}
                  onChange={(e) => setSelectedStatus(e.target.value)}
                  className="block w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm bg-white text-gray-700"
                >
                  {options.map((opt) => (
                    <option key={opt.value} value={opt.value}>
                      {opt.label} ({opt.value})
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <div className="flex justify-end gap-2 pt-2">
              <button
                type="button"
                onClick={onCancel}
                disabled={loading}
                className="px-4 py-2 border border-gray-200 hover:bg-gray-50 text-gray-500 text-xs font-bold rounded-xl transition-colors disabled:opacity-50"
              >
                Cancel
              </button>
              <SubmitButton loading={loading} className="w-auto px-5">
                Update Status
              </SubmitButton>
            </div>
          </form>
        )}
      </div>
    </div>
  );
};

export default StatusUpdateModal;
