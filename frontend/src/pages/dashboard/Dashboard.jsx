import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { 
  Building2, 
  Briefcase, 
  Users, 
  FileText, 
  Calendar, 
  UserCheck, 
  RefreshCw, 
  RotateCcw,
  AlertTriangle,
  WifiOff
} from 'lucide-react';
import axiosClient from '../../services/axiosClient';
import DashboardCard from '../../components/ui/DashboardCard';
import StatisticsCard from '../../components/ui/StatisticsCard';
import SectionTitle from '../../components/ui/SectionTitle';
import LoadingCard from '../../components/ui/LoadingCard';

// ── Mock data used when backend is unreachable ───────────────────────────────
const MOCK_SUMMARY    = { totalCompanies: 12, totalJobs: 34, totalCandidates: 128, totalApplications: 256, totalInterviews: 47, totalUsers: 8 };
const MOCK_APPS       = { totalApplications: 256, applied: 89, reviewing: 62, interview: 47, offer: 28, hired: 19, rejected: 11 };
const MOCK_JOBS       = { openJobs: 21, closedJobs: 9, draftJobs: 4 };
const MOCK_CANDIDATES = { candidatesWithCv: 104, candidatesWithoutCv: 24 };
const MOCK_COMPANIES  = { companiesWithActiveJobs: 9, companiesWithoutJobs: 3 };
// ─────────────────────────────────────────────────────────────────────────────

const Dashboard = () => {
  const { t } = useTranslation();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [refreshing, setRefreshing] = useState(false);
  const [isOffline, setIsOffline] = useState(false);

  // States for API data
  const [summary, setSummary] = useState(null);
  const [apps, setApps] = useState(null);
  const [jobs, setJobs] = useState(null);
  const [candidates, setCandidates] = useState(null);
  const [companies, setCompanies] = useState(null);

  const fetchDashboardData = async (isSilent = false) => {
    if (!isSilent) {
      setLoading(true);
    } else {
      setRefreshing(true);
    }
    setError(null);

    try {
      const [
        summaryRes,
        appsRes,
        jobsRes,
        candidatesRes,
        companiesRes
      ] = await Promise.all([
        axiosClient.get('/dashboard/summary'),
        axiosClient.get('/dashboard/applications'),
        axiosClient.get('/dashboard/jobs'),
        axiosClient.get('/dashboard/candidates'),
        axiosClient.get('/dashboard/companies')
      ]);

      setIsOffline(false);
      setSummary(summaryRes.data.data);
      setApps(appsRes.data.data);
      setJobs(jobsRes.data.data);
      setCandidates(candidatesRes.data.data);
      setCompanies(companiesRes.data.data);
    } catch (err) {
      console.error('Error fetching dashboard data:', err);

      // Network error → use mock data so the UI still renders
      const isNetworkError = !err.response;
      if (isNetworkError) {
        setIsOffline(true);
        setSummary(MOCK_SUMMARY);
        setApps(MOCK_APPS);
        setJobs(MOCK_JOBS);
        setCandidates(MOCK_CANDIDATES);
        setCompanies(MOCK_COMPANIES);
      } else {
        setError(
          err.response?.data?.message ||
          'Failed to connect to the backend server. Please verify that the API is running and check your connection.'
        );
      }
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const handleRefresh = () => {
    fetchDashboardData(true);
  };

  const formatNum = (value) => {
    if (value === undefined || value === null) return '0';
    return new Intl.NumberFormat('en-US').format(value);
  };

  if (loading) {
    return (
      <div className="space-y-8 animate-fadeIn">
        {/* Header Skeleton */}
        <div className="bg-white border border-gray-200 rounded-2xl p-6 md:p-8 shadow-sm flex items-center justify-between">
          <div className="space-y-2.5 w-1/2">
            <div className="h-6 bg-gray-200 rounded-md w-3/4 animate-pulse"></div>
            <div className="h-4 bg-gray-100 rounded-md w-1/2 animate-pulse"></div>
          </div>
          <div className="h-10 bg-gray-200 rounded-xl w-24 animate-pulse"></div>
        </div>

        {/* Top Cards Skeleton Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-5">
          {Array.from({ length: 6 }).map((_, i) => (
            <div key={i} className="bg-white border border-gray-200 rounded-2xl p-5 shadow-sm animate-pulse flex justify-between items-start">
              <div className="space-y-2.5 flex-1">
                <div className="h-3 bg-gray-200 rounded-md w-1/2"></div>
                <div className="h-8 bg-gray-300 rounded-md w-1/3"></div>
              </div>
              <div className="w-10 h-10 bg-gray-100 rounded-xl border border-gray-100"></div>
            </div>
          ))}
        </div>

        {/* Middle Section Skeleton */}
        <div className="bg-white border border-gray-200 rounded-2xl p-6 shadow-sm animate-pulse space-y-4">
          <div className="h-4 bg-gray-200 rounded-md w-1/4"></div>
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
            {Array.from({ length: 6 }).map((_, i) => (
              <div key={i} className="h-20 bg-gray-50 border border-gray-100 rounded-xl"></div>
            ))}
          </div>
        </div>

        {/* Bottom Section Skeleton */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <LoadingCard rows={3} />
          <LoadingCard rows={2} />
          <LoadingCard rows={2} />
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50/70 border border-red-200 rounded-2xl p-8 text-center max-w-2xl mx-auto space-y-5 shadow-sm animate-fadeIn">
        <div className="w-12 h-12 rounded-xl bg-red-100 text-red-600 flex items-center justify-center mx-auto border border-red-200">
          <AlertTriangle className="w-6 h-6" />
        </div>
        <div className="space-y-2">
          <h3 className="text-lg font-bold text-red-800">{t('common.error', 'Connection Error')}</h3>
          <p className="text-sm text-red-600 leading-relaxed max-w-md mx-auto">{error}</p>
        </div>
        <button
          onClick={() => fetchDashboardData(false)}
          className="inline-flex items-center px-4 py-2.5 bg-red-600 hover:bg-red-700 text-white font-semibold text-sm rounded-xl transition-colors shadow-md shadow-red-100 gap-1.5"
        >
          <RotateCcw className="w-4 h-4" /> Retry Connection
        </button>
      </div>
    );
  }

  const getStagePercentage = (stageVal) => {
    if (!apps || apps.totalApplications === 0) return 0;
    return Math.round((stageVal / apps.totalApplications) * 100);
  };

  return (
    <div className="space-y-8 animate-fadeIn">
      {/* Welcome & Refresh Header */}
      <div className="bg-white border border-gray-200 rounded-2xl p-6 md:p-8 shadow-sm flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-gray-800">{t('dashboard.title', 'System Dashboard')}</h2>
          <p className="text-gray-500 mt-1">{t('dashboard.subtitle', 'Real-time aggregate analytics, hiring pipeline metrics, and system quotas.')}</p>
        </div>
        <div className="flex items-center gap-3">
          <button
            onClick={handleRefresh}
            disabled={refreshing}
            className="inline-flex items-center px-4 py-2.5 border border-gray-200 text-gray-600 hover:text-gray-800 font-semibold text-sm rounded-xl hover:bg-gray-50 disabled:opacity-50 transition-all duration-100 gap-1.5 bg-white shadow-sm"
          >
            <RefreshCw className={`w-4 h-4 ${refreshing ? 'animate-spin' : ''}`} />
            {refreshing ? t('common.loading', 'Refreshing...') : 'Refresh Data'}
          </button>
        </div>
      </div>

      {/* Offline banner – shown when backend is unreachable */}
      {isOffline && (
        <div className="flex items-center gap-3 bg-amber-50 border border-amber-200 rounded-2xl px-5 py-3 shadow-sm">
          <WifiOff className="w-5 h-5 text-amber-500 flex-shrink-0" />
          <div className="flex-1 min-w-0">
            <p className="text-sm font-semibold text-amber-800">
              Backend server is offline — showing demo data
            </p>
            <p className="text-xs text-amber-600 mt-0.5">
              Start the backend (port 8080) then click <strong>Refresh Data</strong> to load live data.
            </p>
          </div>
          <button
            onClick={handleRefresh}
            disabled={refreshing}
            className="inline-flex items-center px-3 py-1.5 bg-amber-500 hover:bg-amber-600 text-white text-xs font-semibold rounded-lg gap-1.5 transition-colors disabled:opacity-50 flex-shrink-0"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${refreshing ? 'animate-spin' : ''}`} />
            Retry
          </button>
        </div>
      )}

      {/* Top Section Summary Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-5">
        <DashboardCard 
          name={t('nav.companies', 'Companies')} 
          value={summary?.totalCompanies} 
          icon={Building2} 
          color="blue" 
        />
        <DashboardCard 
          name={t('dashboard.totalJobs', 'Job Postings')} 
          value={summary?.totalJobs} 
          icon={Briefcase} 
          color="indigo" 
        />
        <DashboardCard 
          name={t('dashboard.totalCandidates', 'Candidates')} 
          value={summary?.totalCandidates} 
          icon={Users} 
          color="teal" 
        />
        <DashboardCard 
          name={t('dashboard.totalApplications', 'Applications')} 
          value={summary?.totalApplications} 
          icon={FileText} 
          color="emerald" 
        />
        <DashboardCard 
          name={t('nav.interviews', 'Interviews')} 
          value={summary?.totalInterviews} 
          icon={Calendar} 
          color="amber" 
        />
        <DashboardCard 
          name="Active Users" 
          value={summary?.totalUsers} 
          icon={UserCheck} 
          color="purple" 
        />
      </div>

      {/* Middle Section Application Status Pipeline */}
      <div className="bg-white border border-gray-200 rounded-2xl p-6 shadow-sm space-y-5">
        <SectionTitle 
          description="Progress distribution of candidates across the hiring workflow stages."
        >
          {t('dashboard.pipelineStatus', 'Hiring Pipeline Status')}
        </SectionTitle>

        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4 pt-2">
          {/* APPLIED */}
          <div className="bg-blue-50 border border-blue-100 rounded-2xl p-4 flex flex-col justify-between h-24 hover:shadow-sm transition-all duration-100">
            <span className="text-xs font-bold text-blue-500 uppercase tracking-wider">{t('status.applied', 'Applied')}</span>
            <div className="flex justify-between items-baseline mt-1">
              <span className="text-2xl font-extrabold text-blue-700">{formatNum(apps?.applied)}</span>
              <span className="text-xs font-bold text-blue-600/80">{getStagePercentage(apps?.applied)}%</span>
            </div>
          </div>

          {/* REVIEWING */}
          <div className="bg-indigo-50 border border-indigo-100 rounded-2xl p-4 flex flex-col justify-between h-24 hover:shadow-sm transition-all duration-100">
            <span className="text-xs font-bold text-indigo-500 uppercase tracking-wider">{t('status.screening', 'Reviewing')}</span>
            <div className="flex justify-between items-baseline mt-1">
              <span className="text-2xl font-extrabold text-indigo-700">{formatNum(apps?.reviewing)}</span>
              <span className="text-xs font-bold text-indigo-600/80">{getStagePercentage(apps?.reviewing)}%</span>
            </div>
          </div>

          {/* INTERVIEW */}
          <div className="bg-amber-50 border border-amber-100 rounded-2xl p-4 flex flex-col justify-between h-24 hover:shadow-sm transition-all duration-100">
            <span className="text-xs font-bold text-amber-500 uppercase tracking-wider">{t('status.interviewing', 'Interview')}</span>
            <div className="flex justify-between items-baseline mt-1">
              <span className="text-2xl font-extrabold text-amber-700">{formatNum(apps?.interview)}</span>
              <span className="text-xs font-bold text-amber-600/80">{getStagePercentage(apps?.interview)}%</span>
            </div>
          </div>

          {/* OFFER */}
          <div className="bg-emerald-50 border border-emerald-100 rounded-2xl p-4 flex flex-col justify-between h-24 hover:shadow-sm transition-all duration-100">
            <span className="text-xs font-bold text-emerald-500 uppercase tracking-wider">{t('status.offered', 'Offer')}</span>
            <div className="flex justify-between items-baseline mt-1">
              <span className="text-2xl font-extrabold text-emerald-700">{formatNum(apps?.offer)}</span>
              <span className="text-xs font-bold text-emerald-600/80">{getStagePercentage(apps?.offer)}%</span>
            </div>
          </div>

          {/* HIRED */}
          <div className="bg-teal-50 border border-teal-100 rounded-2xl p-4 flex flex-col justify-between h-24 hover:shadow-sm transition-all duration-100">
            <span className="text-xs font-bold text-teal-500 uppercase tracking-wider">{t('status.hired', 'Hired')}</span>
            <div className="flex justify-between items-baseline mt-1">
              <span className="text-2xl font-extrabold text-teal-700">{formatNum(apps?.hired)}</span>
              <span className="text-xs font-bold text-teal-600/80">{getStagePercentage(apps?.hired)}%</span>
            </div>
          </div>

          {/* REJECTED */}
          <div className="bg-red-50 border border-red-100 rounded-2xl p-4 flex flex-col justify-between h-24 hover:shadow-sm transition-all duration-100">
            <span className="text-xs font-bold text-red-500 uppercase tracking-wider">{t('status.rejected', 'Rejected')}</span>
            <div className="flex justify-between items-baseline mt-1">
              <span className="text-2xl font-extrabold text-red-700">{formatNum(apps?.rejected)}</span>
              <span className="text-xs font-bold text-red-600/80">{getStagePercentage(apps?.rejected)}%</span>
            </div>
          </div>
        </div>
      </div>

      {/* Bottom Section Detailed Statistics */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Job statistics */}
        <StatisticsCard
          title="Job Postings Status"
          entries={[
            { label: 'Open Jobs', value: jobs?.openJobs || 0, color: 'bg-blue-500' },
            { label: 'Closed Jobs', value: jobs?.closedJobs || 0, color: 'bg-gray-400' },
            { label: 'Draft Jobs', value: jobs?.draftJobs || 0, color: 'bg-amber-400' },
          ]}
        />

        {/* Candidate statistics */}
        <StatisticsCard
          title="Candidate Resume Metrics"
          entries={[
            { label: 'Uploaded CV/Resume', value: candidates?.candidatesWithCv || 0, color: 'bg-emerald-500' },
            { label: 'No CV Uploaded', value: candidates?.candidatesWithoutCv || 0, color: 'bg-rose-500' },
          ]}
        />

        {/* Company statistics */}
        <StatisticsCard
          title="Company Job Portfolios"
          entries={[
            { label: 'Companies with active jobs', value: companies?.companiesWithActiveJobs || 0, color: 'bg-indigo-500' },
            { label: 'No active job postings', value: companies?.companiesWithoutJobs || 0, color: 'bg-gray-300' },
          ]}
        />
      </div>
    </div>
  );
};

export default Dashboard;
