import React from 'react';
import { useTranslation } from 'react-i18next';
import { ChevronLeft, ChevronRight } from 'lucide-react';

const Pagination = ({ page, totalPages, totalElements, size, onPageChange, className = '' }) => {
  const { t } = useTranslation();

  if (totalPages <= 1) return null;

  return (
    <div className={`flex flex-col sm:flex-row items-center justify-between gap-4 py-4 px-2 ${className}`}>
      <span className="text-xs font-semibold text-gray-450 uppercase tracking-wider">
        {t('common.showing', 'Showing Page')} {page + 1} {t('common.of', 'of')} {totalPages} ({totalElements} {t('common.results', 'Total Elements')})
      </span>
      
      <div className="flex items-center gap-2">
        <button
          onClick={() => onPageChange(page - 1)}
          disabled={page === 0}
          className="p-2 border border-gray-200 text-gray-600 hover:text-gray-800 font-bold rounded-xl hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed disabled:hover:bg-transparent bg-white shadow-sm transition-all"
          title={t('common.previous', 'Previous Page')}
        >
          <ChevronLeft className="w-4 h-4" />
        </button>
        
        <span className="text-sm font-bold text-gray-700 bg-white border border-gray-200 rounded-xl px-4 py-1.5 shadow-sm">
          {page + 1}
        </span>
        
        <button
          onClick={() => onPageChange(page + 1)}
          disabled={page === totalPages - 1}
          className="p-2 border border-gray-200 text-gray-600 hover:text-gray-800 font-bold rounded-xl hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed disabled:hover:bg-transparent bg-white shadow-sm transition-all"
          title={t('common.next', 'Next Page')}
        >
          <ChevronRight className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
};

export default Pagination;
