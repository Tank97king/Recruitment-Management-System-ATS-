import React from 'react';
import { useTranslation } from 'react-i18next';

const StatusBadge = ({ status }) => {
  const { t } = useTranslation();

  const statusStyles = {
    // Job statuses
    OPEN:       'bg-emerald-50 text-emerald-700 border-emerald-100',
    ACTIVE:     'bg-emerald-50 text-emerald-700 border-emerald-100',
    CLOSED:     'bg-gray-100 text-gray-500 border-gray-200',
    DRAFT:      'bg-amber-50 text-amber-700 border-amber-100',

    // Application pipeline statuses
    APPLIED:    'bg-blue-50 text-blue-700 border-blue-100',
    REVIEWING:  'bg-indigo-50 text-indigo-700 border-indigo-100',
    INTERVIEW:  'bg-purple-50 text-purple-700 border-purple-100',
    OFFER:      'bg-pink-50 text-pink-700 border-pink-100',
    HIRED:      'bg-teal-50 text-teal-700 border-teal-100',
    REJECTED:   'bg-red-50 text-red-700 border-red-100',
    WITHDRAWN:  'bg-orange-50 text-orange-700 border-orange-100',

    // Interview statuses
    SCHEDULED:  'bg-sky-50 text-sky-700 border-sky-100',
    COMPLETED:  'bg-emerald-50 text-emerald-700 border-emerald-100',
    CANCELLED:  'bg-rose-50 text-rose-700 border-rose-100',

    // Interview types
    ONLINE:     'bg-blue-50 text-blue-700 border-blue-100',
    ONSITE:     'bg-violet-50 text-violet-700 border-violet-100',
  };

  const defaultLabel = status
    ? status.charAt(0).toUpperCase() + status.slice(1).toLowerCase()
    : 'Unknown';

  const translatedLabel = status ? t(`status.${status}`, defaultLabel) : defaultLabel;

  return (
    <span
      className={`inline-flex items-center px-2.5 py-1 text-xs font-semibold rounded-full border ${
        statusStyles[status] || 'bg-gray-50 text-gray-400 border-gray-100'
      }`}
    >
      {translatedLabel}
    </span>
  );
};

export default StatusBadge;
