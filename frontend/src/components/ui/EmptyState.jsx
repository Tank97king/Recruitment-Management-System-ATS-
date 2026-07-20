import React from 'react';
import { useTranslation } from 'react-i18next';
import { AlertCircle } from 'lucide-react';

const EmptyState = ({ message, className = '' }) => {
  const { t } = useTranslation();
  const resolvedMessage = message || t('common.noData', 'No data available');

  return (
    <div className={`bg-white border border-gray-200 rounded-2xl p-8 text-center flex flex-col items-center justify-center shadow-sm ${className}`}>
      <AlertCircle className="w-10 h-10 text-gray-300" />
      <p className="text-sm font-semibold text-gray-500 mt-3">{resolvedMessage}</p>
    </div>
  );
};

export default EmptyState;
