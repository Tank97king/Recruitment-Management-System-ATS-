import React, { useState, useEffect, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import { toast } from 'react-hot-toast';
import { SlidersHorizontal, RotateCcw, LayoutGrid, Loader2, Filter, Briefcase } from 'lucide-react';
import axiosClient from '../../services/axiosClient';
import PipelineBoard from './components/PipelineBoard';
import SummaryCard from './components/SummaryCard';

const Pipeline = () => {
  const { t } = useTranslation();
  const [loading, setLoading] = useState(true);
  const [summaryData, setSummaryData] = useState(null);
  const [pipelineData, setPipelineData] = useState({});

  // Filters state
  const [companyFilter, setCompanyFilter] = useState('');
  const [jobFilter, setJobFilter] = useState('');

  // Dropdown lists
  const [companies, setCompanies] = useState([]);
  const [jobs, setJobs] = useState([]);

  // Fetch companies and jobs for filter options
  useEffect(() => {
    const fetchDropdowns = async () => {
      try {
        const [compRes, jobsRes] = await Promise.all([
          axiosClient.get('/companies', { params: { page: 0, size: 500 } }),
          axiosClient.get('/jobs', { params: { page: 0, size: 500 } })
        ]);
        setCompanies(compRes.data.data.content || []);
        setJobs(jobsRes.data.data.content || []);
      } catch (err) {
        console.error('Error fetching pipeline filter lists:', err);
      }
    };
    fetchDropdowns();
  }, []);

  const fetchPipelineAndSummary = useCallback(async () => {
    setLoading(true);
    try {
      const params = {
        companyId: companyFilter || undefined,
        jobId: jobFilter || undefined,
      };

      const [pipeRes, summaryRes] = await Promise.all([
        axiosClient.get('/pipeline', { params }),
        axiosClient.get('/pipeline/summary', { params })
      ]);

      // Normalize pipelineResponse mapping backend columns (stage, applications) to keyed object
      const rawColumns = pipeRes.data.data.columns || [];
      const boardObj = {};
      rawColumns.forEach((col) => {
        boardObj[col.stage] = col.applications || [];
      });

      setPipelineData(boardObj);
      setSummaryData(summaryRes.data.data);
    } catch (err) {
      console.error('Error loading pipeline dashboard:', err);
      toast.error(t('common.error', 'Failed to load recruitment pipeline details.'));
    } finally {
      setLoading(false);
    }
  }, [companyFilter, jobFilter, t]);

  useEffect(() => {
    fetchPipelineAndSummary();
  }, [fetchPipelineAndSummary]);

  const handleMoveApplication = async (applicationId, targetStatus) => {
    try {
      await axiosClient.patch(`/pipeline/applications/${applicationId}/status`, {
        status: targetStatus
      });
      toast.success(t('common.success', 'Candidate stage updated successfully!'));
      fetchPipelineAndSummary();
    } catch (err) {
      console.error('Error updating candidate stage:', err);
      const msg = err.response?.data?.message || t('common.error', 'Invalid stage transition attempted.');
      toast.error(msg);
    }
  };

  return (
    <div className="space-y-6 animate-fadeIn">
      {/* Header section */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-gray-800 tracking-tight">{t('pipeline.title', 'Recruitment Pipeline')}</h2>
          <p className="text-gray-500 mt-1">{t('pipeline.subtitle', 'Review applicant screening stages and track recruitment operations.')}</p>
        </div>
      </div>

      {/* Filter panel */}
      <div className="bg-white border border-gray-200 rounded-2xl p-5 shadow-sm space-y-4">
        <div className="flex items-center gap-2 text-gray-800 border-b border-gray-100 pb-3">
          <SlidersHorizontal className="w-4 h-4 text-blue-500" />
          <h4 className="font-bold text-sm uppercase tracking-wider">{t('common.filter', 'Board Filters')}</h4>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          {/* Company filter */}
          <div className="relative">
            <Filter className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
            <select
              value={companyFilter}
              onChange={(e) => setCompanyFilter(e.target.value)}
              className="w-full pl-10 pr-4 py-2.5 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 text-xs font-semibold bg-white text-gray-600 appearance-none"
            >
              <option value="">{t('pipeline.allCompanies', 'All Partner Companies')}</option>
              {companies.map(c => (
                <option key={c.id} value={c.id}>{c.companyName || c.name}</option>
              ))}
            </select>
          </div>

          <div className="relative">
            <Briefcase className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
            <select
              value={jobFilter}
              onChange={(e) => setJobFilter(e.target.value)}
              className="w-full pl-10 pr-4 py-2.5 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 text-xs font-semibold bg-white text-gray-600 appearance-none"
            >
              <option value="">{t('pipeline.allJobs', 'All Active Openings')}</option>
              {jobs.map(j => (
                <option key={j.id} value={j.id}>{j.title}</option>
              ))}
            </select>
          </div>

          {(companyFilter || jobFilter) && (
            <button
              onClick={() => { setCompanyFilter(''); setJobFilter(''); }}
              className="inline-flex items-center gap-1 text-xs font-bold text-gray-400 hover:text-gray-600 transition-colors"
            >
              <RotateCcw className="w-3.5 h-3.5" /> {t('pipeline.resetFilters', 'Reset Board Filters')}
            </button>
          )}
        </div>
      </div>

      {/* Summary statistics cards */}
      {loading && !summaryData ? (
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-7 gap-4">
          {[...Array(7)].map((_, idx) => (
            <div key={idx} className="h-20 bg-gray-100 border border-gray-200 rounded-2xl animate-pulse" />
          ))}
        </div>
      ) : (
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-7 gap-4">
          <SummaryCard 
            title={t('dashboard.totalApplications', 'Total Apps')} 
            count={summaryData?.totalApplications || 0} 
            colorClass="text-gray-800" 
            borderClass="border-l-gray-400" 
          />
          <SummaryCard 
            title={t('pipeline.applied', 'Applied')} 
            count={summaryData?.applied || 0} 
            colorClass="text-blue-700" 
            borderClass="border-l-blue-500" 
          />
          <SummaryCard 
            title={t('pipeline.screening', 'Reviewing')} 
            count={summaryData?.reviewing || 0} 
            colorClass="text-amber-700" 
            borderClass="border-l-amber-500" 
          />
          <SummaryCard 
            title={t('pipeline.interview', 'Interview')} 
            count={summaryData?.interview || 0} 
            colorClass="text-indigo-700" 
            borderClass="border-l-indigo-500" 
          />
          <SummaryCard 
            title={t('pipeline.offer', 'Offer')} 
            count={summaryData?.offer || 0} 
            colorClass="text-pink-700" 
            borderClass="border-l-pink-500" 
          />
          <SummaryCard 
            title={t('pipeline.hired', 'Hired')} 
            count={summaryData?.hired || 0} 
            colorClass="text-emerald-700" 
            borderClass="border-l-emerald-500" 
          />
          <SummaryCard 
            title={t('pipeline.rejected', 'Rejected')} 
            count={summaryData?.rejected || 0} 
            colorClass="text-red-700" 
            borderClass="border-l-red-500" 
          />
        </div>
      )}

      {/* Kanban Board Container */}
      {loading ? (
        <div className="bg-white border border-gray-200 rounded-2xl p-8 flex flex-col items-center justify-center min-h-[400px] gap-3 text-gray-400 font-semibold text-sm">
          <Loader2 className="w-8 h-8 animate-spin text-blue-500" />
          <span>{t('common.loading', 'Synchronizing recruitment board stages...')}</span>
        </div>
      ) : (
        <PipelineBoard 
          pipelineData={pipelineData} 
          onMoveApplication={handleMoveApplication} 
        />
      )}
    </div>
  );
};

export default Pipeline;
