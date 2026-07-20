import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { Search, Building2, Briefcase, MapPin, SlidersHorizontal, RotateCcw } from 'lucide-react';
import axiosClient from '../../../services/axiosClient';

const FilterPanel = ({ onFilterChange }) => {
  const { t } = useTranslation();
  const [keyword, setKeyword] = useState('');
  const [companyId, setCompanyId] = useState('');
  const [status, setStatus] = useState('');
  const [employmentType, setEmploymentType] = useState('');
  const [location, setLocation] = useState('');
  const [companies, setCompanies] = useState([]);

  useEffect(() => {
    const fetchCompanies = async () => {
      try {
        const response = await axiosClient.get('/companies', {
          params: { page: 0, size: 100 }
        });
        setCompanies(response.data.data.content || []);
      } catch (err) {
        console.error('Error fetching companies for filter dropdown:', err);
      }
    };
    fetchCompanies();
  }, []);

  useEffect(() => {
    onFilterChange({
      keyword,
      companyId,
      status,
      employmentType,
      location
    });
  }, [keyword, companyId, status, employmentType, location, onFilterChange]);

  const handleReset = () => {
    setKeyword('');
    setCompanyId('');
    setStatus('');
    setEmploymentType('');
    setLocation('');
  };

  return (
    <div className="bg-white border border-gray-200 rounded-2xl p-5 shadow-sm space-y-4">
      <div className="flex items-center gap-2 text-gray-800 border-b border-gray-100 pb-3">
        <SlidersHorizontal className="w-4 h-4 text-blue-500" />
        <h4 className="font-bold text-sm uppercase tracking-wider">{t('common.filter', 'Search & Filters')}</h4>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4">
        {/* Keyword Search */}
        <div className="relative">
          <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            type="text"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            placeholder={t('common.search', 'Search title/keywords...')}
            className="w-full pl-10 pr-4 py-2.5 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 text-xs font-semibold bg-gray-50/50"
          />
        </div>

        {/* Company Filter */}
        <div className="relative">
          <Building2 className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <select
            value={companyId}
            onChange={(e) => setCompanyId(e.target.value)}
            className="w-full pl-10 pr-4 py-2.5 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 text-xs font-semibold bg-white text-gray-600 appearance-none"
          >
            <option value="">{t('jobs.allCompanies', 'All Companies')}</option>
            {companies.map((c) => (
              <option key={c.id} value={c.id}>
                {c.companyName}
              </option>
            ))}
          </select>
        </div>

        {/* Status Filter */}
        <div className="relative">
          <Briefcase className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <select
            value={status}
            onChange={(e) => setStatus(e.target.value)}
            className="w-full pl-10 pr-4 py-2.5 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 text-xs font-semibold bg-white text-gray-600 appearance-none"
          >
            <option value="">{t('jobs.allStatuses', 'All Statuses')}</option>
            <option value="OPEN">{t('status.ACTIVE', 'Open')}</option>
            <option value="DRAFT">{t('status.DRAFT', 'Draft')}</option>
            <option value="CLOSED">{t('status.CLOSED', 'Closed')}</option>
          </select>
        </div>

        {/* Employment Type */}
        <div className="relative">
          <Briefcase className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <select
            value={employmentType}
            onChange={(e) => setEmploymentType(e.target.value)}
            className="w-full pl-10 pr-4 py-2.5 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 text-xs font-semibold bg-white text-gray-600 appearance-none"
          >
            <option value="">{t('jobs.jobType', 'All Job Types')}</option>
            <option value="FULL_TIME">Full-time</option>
            <option value="PART_TIME">Part-time</option>
            <option value="CONTRACT">Contract</option>
            <option value="FREELANCE">Freelance</option>
            <option value="INTERNSHIP">Internship</option>
          </select>
        </div>

        {/* Location filter */}
        <div className="relative">
          <MapPin className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            type="text"
            value={location}
            onChange={(e) => setLocation(e.target.value)}
            placeholder={t('jobs.location', 'Location filter...')}
            className="w-full pl-10 pr-4 py-2.5 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 text-xs font-semibold bg-gray-50/50"
          />
        </div>
      </div>

      <div className="flex justify-end pt-1">
        <button
          onClick={handleReset}
          className="inline-flex items-center gap-1 text-xs font-bold text-gray-400 hover:text-gray-600 transition-colors"
        >
          <RotateCcw className="w-3.5 h-3.5" /> {t('pipeline.resetFilters', 'Reset Filters')}
        </button>
      </div>
    </div>
  );
};

export default FilterPanel;
