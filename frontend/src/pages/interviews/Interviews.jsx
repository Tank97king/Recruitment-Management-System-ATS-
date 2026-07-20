import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { toast } from 'react-hot-toast';
import { Plus, Eye, Edit2, Trash2, ArrowUpDown, Calendar, Search, SlidersHorizontal, RotateCcw, Video, MapPin, Phone, Briefcase } from 'lucide-react';
import axiosClient from '../../services/axiosClient';
import DataTable from '../../components/ui/DataTable';
import Pagination from '../../components/ui/Pagination';
import ConfirmDialog from '../../components/ui/ConfirmDialog';
import StatusBadge from '../../components/ui/StatusBadge';
import InterviewStatusModal from './components/InterviewStatusModal';

const Interviews = () => {
  const { t } = useTranslation();
  const [loading, setLoading] = useState(true);
  const [interviews, setInterviews] = useState([]);
  const [companies, setCompanies] = useState([]);

  // Search & Filter state
  const [searchKeyword, setSearchKeyword] = useState('');
  const [companyFilter, setCompanyFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [typeFilter, setTypeFilter] = useState('');
  const [dateFilter, setDateFilter] = useState('');

  // Pagination & Sorting state
  const [page, setPage] = useState(0);
  const [size] = useState(10);
  const [sortBy, setSortBy] = useState('interviewDate');
  const [sortDirection, setSortDirection] = useState('asc');

  // Metadata
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  // Modals state
  const [deleteOpen, setDeleteOpen] = useState(false);
  const [interviewToDelete, setInterviewToDelete] = useState(null);
  const [deleteLoading, setDeleteLoading] = useState(false);

  // Status Modal state
  const [statusModalOpen, setStatusModalOpen] = useState(false);
  const [interviewForStatusUpdate, setInterviewForStatusUpdate] = useState(null);
  const [statusLoading, setStatusLoading] = useState(false);

  // Fetch companies list for filter dropdown
  useEffect(() => {
    const fetchCompanies = async () => {
      try {
        const response = await axiosClient.get('/companies', {
          params: { page: 0, size: 100 }
        });
        setCompanies(response.data.data.content || []);
      } catch (err) {
        console.error('Error fetching companies:', err);
      }
    };
    fetchCompanies();
  }, []);

  const fetchInterviews = useCallback(async () => {
    setLoading(true);
    try {
      const response = await axiosClient.get('/interviews', {
        params: {
          companyId: companyFilter || undefined,
          interviewStatus: statusFilter || undefined,
          interviewType: typeFilter || undefined,
          interviewDate: dateFilter || undefined,
          page,
          size,
          sortBy,
          sortDirection,
        },
      });
      const pageData = response.data.data;
      setInterviews(pageData.content || []);
      setTotalPages(pageData.totalPages || 1);
      setTotalElements(pageData.totalElements || 0);
    } catch (err) {
      console.error('Error fetching interviews:', err);
      toast.error(t('common.error', 'Failed to load interview sessions.'));
    } finally {
      setLoading(false);
    }
  }, [companyFilter, statusFilter, typeFilter, dateFilter, page, size, sortBy, sortDirection, t]);

  useEffect(() => {
    fetchInterviews();
  }, [fetchInterviews]);

  const handleSort = (field) => {
    if (sortBy === field) {
      setSortDirection(prev => (prev === 'asc' ? 'desc' : 'asc'));
    } else {
      setSortBy(field);
      setSortDirection('asc');
    }
    setPage(0);
  };

  const handleResetFilters = () => {
    setSearchKeyword('');
    setCompanyFilter('');
    setStatusFilter('');
    setTypeFilter('');
    setDateFilter('');
    setPage(0);
  };

  const handleDeleteClick = (interview) => {
    setInterviewToDelete(interview);
    setDeleteOpen(true);
  };

  const handleDeleteConfirm = async () => {
    if (!interviewToDelete) return;
    setDeleteLoading(true);
    try {
      await axiosClient.delete(`/interviews/${interviewToDelete.id}`);
      toast.success(t('common.success', 'Interview scheduled session deleted successfully!'));
      setDeleteOpen(false);
      setInterviewToDelete(null);
      
      if (interviews.length === 1 && page > 0) {
        setPage(prev => prev - 1);
      } else {
        fetchInterviews();
      }
    } catch (err) {
      console.error('Error deleting interview:', err);
      const msg = err.response?.data?.message || t('common.error', 'Failed to delete interview session.');
      toast.error(msg);
    } finally {
      setDeleteLoading(false);
    }
  };

  const openStatusModal = (interview) => {
    setInterviewForStatusUpdate(interview);
    setStatusModalOpen(true);
  };

  const handleUpdateStatusConfirm = async (nextStatus) => {
    if (!interviewForStatusUpdate) return;
    setStatusLoading(true);
    try {
      await axiosClient.patch(`/interviews/${interviewForStatusUpdate.id}/status`, {
        status: nextStatus
      });
      toast.success(t('common.success', 'Interview status updated successfully!'));
      setStatusModalOpen(false);
      setInterviewForStatusUpdate(null);
      fetchInterviews();
    } catch (err) {
      console.error('Error updating status:', err);
      const msg = err.response?.data?.message || t('common.error', 'Failed to update interview status.');
      toast.error(msg);
    } finally {
      setStatusLoading(false);
    }
  };

  const formatDate = (isoString) => {
    if (!isoString) return 'N/A';
    return new Date(isoString).toLocaleDateString();
  };

  const formatTime = (isoString) => {
    if (!isoString) return 'N/A';
    return new Date(isoString).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  };

  // Client-side text filters matching Candidate, Job title, or Interviewer name
  const filteredInterviews = interviews.filter(interview => {
    if (searchKeyword.trim() !== '') {
      const query = searchKeyword.toLowerCase().trim();
      const matchCandidate = interview.candidateName?.toLowerCase().includes(query);
      const matchJob = interview.jobTitle?.toLowerCase().includes(query);
      const matchInterviewer = interview.interviewerName?.toLowerCase().includes(query);
      if (!matchCandidate && !matchJob && !matchInterviewer) return false;
    }
    return true;
  });

  const columns = [
    {
      header: t('interviews.candidate', 'Candidate'),
      cell: (row) => (
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-lg bg-blue-50 text-blue-700 flex items-center justify-center font-bold text-xs border border-blue-100 uppercase flex-shrink-0">
            {row.candidateName?.charAt(0) || 'C'}
          </div>
          <span className="font-bold text-gray-800">{row.candidateName}</span>
        </div>
      )
    },
    {
      header: t('interviews.job', 'Job Title'),
      cell: (row) => <span className="font-bold text-gray-800">{row.jobTitle}</span>
    },
    {
      header: t('jobs.company', 'Company'),
      cell: (row) => <span className="font-semibold text-gray-600">{row.companyName}</span>
    },
    {
      header: (
        <button 
          onClick={() => handleSort('interviewDate')} 
          className="flex items-center gap-1 hover:text-gray-700 transition-colors uppercase tracking-wider font-bold"
        >
          {t('interviews.dateTime', 'Date & Time')} <ArrowUpDown className="w-3.5 h-3.5" />
        </button>
      ),
      cell: (row) => (
        <div>
          <span className="text-gray-700 font-bold block">{formatDate(row.interviewDate)}</span>
          <span className="text-[10px] text-gray-400 font-bold block mt-0.5">{formatTime(row.interviewDate)}</span>
        </div>
      )
    },
    {
      header: t('interviews.interviewType', 'Mode'),
      cell: (row) => {
        const isOnline = row.interviewType === 'ONLINE' || row.interviewType === 'VIDEO_CALL';
        return (
          <span className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold border ${
            isOnline 
              ? 'bg-blue-50 text-blue-700 border-blue-100' 
              : 'bg-purple-50 text-purple-700 border-purple-100'
          }`}>
            {isOnline ? <Video className="w-3 h-3" /> : <MapPin className="w-3 h-3" />}
            {isOnline ? t('interviews.typeVideo', 'Online') : t('interviews.typeOnsite', 'Onsite')}
          </span>
        );
      }
    },
    {
      header: t('interviews.interviewer', 'Interviewer'),
      cell: (row) => (
        <div>
          <span className="text-gray-700 font-bold block">{row.interviewerName}</span>
          <span className="text-[10px] text-gray-400 font-bold block truncate max-w-[150px] mt-0.5">
            {row.interviewerEmail}
          </span>
        </div>
      )
    },
    {
      header: (
        <button 
          onClick={() => handleSort('status')} 
          className="flex items-center gap-1 hover:text-gray-700 transition-colors uppercase tracking-wider font-bold"
        >
          {t('common.status', 'Status')} <ArrowUpDown className="w-3.5 h-3.5" />
        </button>
      ),
      cell: (row) => <StatusBadge status={row.status} />
    },
    {
      header: t('common.actions', 'Actions'),
      cellClassName: 'text-right',
      cell: (row) => {
        const isScheduled = row.status === 'SCHEDULED';
        return (
          <div className="flex items-center justify-end gap-1.5">
            <Link 
              to={`/interviews/${row.id}`} 
              className="p-1.5 rounded-xl text-gray-400 hover:text-blue-600 hover:bg-blue-50 border border-gray-100 hover:border-blue-100 transition-colors"
              title={t('common.view', 'View Details')}
            >
              <Eye className="w-3.5 h-3.5" />
            </Link>
            {isScheduled ? (
              <>
                <Link 
                  to={`/interviews/${row.id}/edit`} 
                  className="p-1.5 rounded-xl text-gray-400 hover:text-indigo-600 hover:bg-indigo-50 border border-gray-100 hover:border-indigo-100 transition-colors"
                  title={t('common.edit', 'Edit scheduled coordinates')}
                >
                  <Edit2 className="w-3.5 h-3.5" />
                </Link>
                <button 
                  onClick={() => openStatusModal(row)}
                  className="p-1.5 rounded-xl text-gray-400 hover:text-amber-600 hover:bg-amber-50 border border-gray-100 hover:border-amber-100 transition-colors"
                  title={t('interviews.editInterview', 'Update Status')}
                >
                  <SlidersHorizontal className="w-3.5 h-3.5" />
                </button>
              </>
            ) : (
              <div className="w-[62px]" />
            )}
            <button 
              onClick={() => handleDeleteClick(row)}
              className="p-1.5 rounded-xl text-gray-400 hover:text-red-600 hover:bg-red-50 border border-gray-100 hover:border-red-100 transition-colors"
              title={t('common.delete', 'Delete session')}
            >
              <Trash2 className="w-3.5 h-3.5" />
            </button>
          </div>
        );
      }
    }
  ];

  return (
    <div className="space-y-6 animate-fadeIn">
      {/* Header controls */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-gray-800 tracking-tight">{t('interviews.title', 'Interviews')}</h2>
          <p className="text-gray-500 mt-1">{t('interviews.subtitle', 'Schedule evaluation sessions, assign interviewers, and review comments.')}</p>
        </div>
        <Link 
          to="/interviews/new"
          className="inline-flex items-center px-4 py-2.5 bg-blue-600 text-white font-semibold text-sm rounded-xl hover:bg-blue-700 transition-colors shadow-md shadow-blue-100 gap-1.5 self-start sm:self-auto"
        >
          <Plus className="w-4 h-4" /> {t('interviews.scheduleInterview', 'Schedule Interview')}
        </Link>
      </div>

      {/* Advanced filter panels */}
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
              value={searchKeyword}
              onChange={(e) => setSearchKeyword(e.target.value)}
              placeholder={t('common.search', 'Search candidate or job...')}
              className="w-full pl-10 pr-4 py-2.5 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 text-xs font-semibold bg-gray-50/50"
            />
          </div>

          {/* Company filter */}
          <div className="relative">
            <SlidersHorizontal className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
            <select
              value={companyFilter}
              onChange={(e) => { setCompanyFilter(e.target.value); setPage(0); }}
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

          {/* Mode/Type filter */}
          <div className="relative">
            <SlidersHorizontal className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
            <select
              value={typeFilter}
              onChange={(e) => { setTypeFilter(e.target.value); setPage(0); }}
              className="w-full pl-10 pr-4 py-2.5 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 text-xs font-semibold bg-white text-gray-600 appearance-none"
            >
              <option value="">{t('interviews.interviewType', 'All Interview Types')}</option>
              <option value="ONLINE">{t('interviews.typeVideo', 'Online Video Call')}</option>
              <option value="ONSITE">{t('interviews.typeOnsite', 'Onsite Office Meeting')}</option>
            </select>
          </div>

          {/* Status Filter */}
          <div className="relative">
            <SlidersHorizontal className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
            <select
              value={statusFilter}
              onChange={(e) => { setStatusFilter(e.target.value); setPage(0); }}
              className="w-full pl-10 pr-4 py-2.5 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 text-xs font-semibold bg-white text-gray-600 appearance-none"
            >
              <option value="">{t('jobs.allStatuses', 'All Statuses')}</option>
              <option value="SCHEDULED">{t('status.SCHEDULED', 'Scheduled')}</option>
              <option value="COMPLETED">{t('status.COMPLETED', 'Completed')}</option>
              <option value="CANCELLED">{t('status.CANCELLED', 'Cancelled')}</option>
            </select>
          </div>

          {/* Date Filter */}
          <div className="relative">
            <Calendar className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
            <input
              type="date"
              value={dateFilter}
              onChange={(e) => { setDateFilter(e.target.value); setPage(0); }}
              className="w-full pl-10 pr-4 py-2.5 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 text-xs font-semibold bg-gray-50"
            />
          </div>
        </div>

        <div className="flex justify-end pt-1">
          <button
            onClick={handleResetFilters}
            className="inline-flex items-center gap-1 text-xs font-bold text-gray-400 hover:text-gray-600 transition-colors"
          >
            <RotateCcw className="w-3.5 h-3.5" /> {t('pipeline.resetFilters', 'Reset Filters')}
          </button>
        </div>
      </div>

      {/* Interviews Table grid */}
      <DataTable 
        columns={columns} 
        data={filteredInterviews} 
        loading={loading}
        emptyMessage={
          <div className="text-center py-8">
            <Calendar className="w-12 h-12 text-gray-300 mx-auto" />
            <h3 className="font-bold text-gray-700 mt-4">{t('common.noData', 'No Scheduled Interviews')}</h3>
          </div>
        }
      />

      {/* Pagination Footer */}
      <Pagination 
        page={page} 
        totalPages={totalPages} 
        totalElements={totalElements} 
        size={size} 
        onPageChange={setPage} 
      />

      {/* Deletion Dialog */}
      <ConfirmDialog 
        open={deleteOpen}
        title={t('interviews.deleteTitle', 'Delete Interview Session')}
        message={t('interviews.deleteConfirm', 'Are you sure you want to delete this interview session?')}
        onConfirm={handleDeleteConfirm}
        onCancel={() => {
          setDeleteOpen(false);
          setInterviewToDelete(null);
        }}
        confirmText={t('common.delete', 'Delete')}
        loading={deleteLoading}
      />

      {/* Status Modal overlay */}
      <InterviewStatusModal
        open={statusModalOpen}
        currentStatus={interviewForStatusUpdate?.status}
        onConfirm={handleUpdateStatusConfirm}
        onCancel={() => {
          setStatusModalOpen(false);
          setInterviewForStatusUpdate(null);
        }}
        loading={statusLoading}
      />
    </div>
  );
};

export default Interviews;
