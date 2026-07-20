import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { toast } from 'react-hot-toast';
import { Plus, Eye, Edit2, Trash2, ArrowUpDown, FileText, Search, SlidersHorizontal, RotateCcw } from 'lucide-react';
import axiosClient from '../../services/axiosClient';
import DataTable from '../../components/ui/DataTable';
import Pagination from '../../components/ui/Pagination';
import ConfirmDialog from '../../components/ui/ConfirmDialog';
import StatusBadge from '../../components/ui/StatusBadge';
import StatusUpdateModal from './components/StatusUpdateModal';

const Applications = () => {
  const { t } = useTranslation();
  const [loading, setLoading] = useState(true);
  const [apps, setApps] = useState([]);
  const [companies, setCompanies] = useState([]);

  // Search & Filter state
  const [searchKeyword, setSearchKeyword] = useState('');
  const [companyFilter, setCompanyFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  // Pagination & Sorting state
  const [page, setPage] = useState(0);
  const [size] = useState(10);
  const [sortBy, setSortBy] = useState('appliedAt');
  const [sortDirection, setSortDirection] = useState('desc');

  // Metadata
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  // Modals state
  const [deleteOpen, setDeleteOpen] = useState(false);
  const [appToDelete, setAppToDelete] = useState(null);
  const [deleteLoading, setDeleteLoading] = useState(false);

  // Status Modal state
  const [statusModalOpen, setStatusModalOpen] = useState(false);
  const [appForStatusUpdate, setAppForStatusUpdate] = useState(null);
  const [statusLoading, setStatusLoading] = useState(false);

  // Fetch partner companies for the filter dropdown
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

  const fetchApplications = useCallback(async () => {
    setLoading(true);
    try {
      const response = await axiosClient.get('/job-applications', {
        params: {
          keyword: searchKeyword.trim() || undefined,
          companyId: companyFilter || undefined,
          status: statusFilter || undefined,
          page,
          size,
          sortBy,
          sortDirection,
        },
      });
      const pageData = response.data.data;
      setApps(pageData.content || []);
      setTotalPages(pageData.totalPages || 1);
      setTotalElements(pageData.totalElements || 0);
    } catch (err) {
      console.error('Error fetching applications:', err);
      toast.error(t('common.error', 'Failed to load job applications.'));
    } finally {
      setLoading(false);
    }
  }, [searchKeyword, companyFilter, statusFilter, page, size, sortBy, sortDirection, t]);

  useEffect(() => {
    fetchApplications();
  }, [fetchApplications]);

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
    setPage(0);
  };

  const handleDeleteClick = (app) => {
    setAppToDelete(app);
    setDeleteOpen(true);
  };

  const handleDeleteConfirm = async () => {
    if (!appToDelete) return;
    setDeleteLoading(true);
    try {
      await axiosClient.delete(`/job-applications/${appToDelete.id}`);
      toast.success(t('common.success', 'Job application deleted successfully!'));
      setDeleteOpen(false);
      setAppToDelete(null);
      
      if (apps.length === 1 && page > 0) {
        setPage(prev => prev - 1);
      } else {
        fetchApplications();
      }
    } catch (err) {
      console.error('Error deleting application:', err);
      const msg = err.response?.data?.message || t('common.error', 'Failed to delete application.');
      toast.error(msg);
    } finally {
      setDeleteLoading(false);
    }
  };

  const openStatusModal = (app) => {
    setAppForStatusUpdate(app);
    setStatusModalOpen(true);
  };

  const handleUpdateStatusConfirm = async (nextStatus) => {
    if (!appForStatusUpdate) return;
    setStatusLoading(true);
    try {
      await axiosClient.put(`/job-applications/${appForStatusUpdate.id}/status`, {
        status: nextStatus
      });
      toast.success(t('common.success', 'Application status updated successfully!'));
      setStatusModalOpen(false);
      setAppForStatusUpdate(null);
      fetchApplications();
    } catch (err) {
      console.error('Error updating status:', err);
      const msg = err.response?.data?.message || t('common.error', 'Failed to update status.');
      toast.error(msg);
    } finally {
      setStatusLoading(false);
    }
  };

  const formatDate = (isoString) => {
    if (!isoString) return 'N/A';
    return new Date(isoString).toLocaleDateString();
  };

  const columns = [
    {
      header: t('applications.candidate', 'Candidate'),
      cell: (row) => (
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-lg bg-blue-50 text-blue-700 flex items-center justify-center font-bold text-xs border border-blue-100 uppercase flex-shrink-0">
            {row.candidateName.charAt(0)}
          </div>
          <div>
            <Link to={`/candidates/${row.candidateId}`} className="font-bold text-gray-800 hover:text-blue-600 transition-colors">
              {row.candidateName}
            </Link>
          </div>
        </div>
      )
    },
    {
      header: t('applications.job', 'Job Title'),
      cell: (row) => (
        <Link to={`/jobs/${row.jobId}`} className="font-bold text-gray-800 hover:text-blue-600 transition-colors">
          {row.jobTitle}
        </Link>
      )
    },
    {
      header: t('jobs.company', 'Company'),
      cell: (row) => <span className="font-semibold text-gray-600">{row.companyName}</span>
    },
    {
      header: (
        <button 
          onClick={() => handleSort('applicationStatus')} 
          className="flex items-center gap-1 hover:text-gray-700 transition-colors uppercase tracking-wider font-bold"
        >
          {t('applications.currentStage', 'Current Status')} <ArrowUpDown className="w-3.5 h-3.5" />
        </button>
      ),
      cell: (row) => <StatusBadge status={row.applicationStatus} />
    },
    {
      header: (
        <button 
          onClick={() => handleSort('appliedAt')} 
          className="flex items-center gap-1 hover:text-gray-700 transition-colors uppercase tracking-wider font-bold"
        >
          {t('applications.appliedDate', 'Applied Date')} <ArrowUpDown className="w-3.5 h-3.5" />
        </button>
      ),
      cell: (row) => <span className="text-gray-500 font-medium">{formatDate(row.appliedAt)}</span>
    },
    {
      header: (
        <button 
          onClick={() => handleSort('updatedAt')} 
          className="flex items-center gap-1 hover:text-gray-700 transition-colors uppercase tracking-wider font-bold"
        >
          {t('applications.appliedDate', 'Last Updated')} <ArrowUpDown className="w-3.5 h-3.5" />
        </button>
      ),
      cell: (row) => <span className="text-gray-500 font-medium">{formatDate(row.updatedAt)}</span>
    },
    {
      header: t('common.actions', 'Actions'),
      cellClassName: 'text-right',
      cell: (row) => {
        const isTerminal = ['HIRED', 'REJECTED', 'WITHDRAWN'].includes(row.applicationStatus);
        return (
          <div className="flex items-center justify-end gap-1.5">
            <Link 
              to={`/applications/${row.id}`} 
              className="p-1.5 rounded-xl text-gray-400 hover:text-blue-600 hover:bg-blue-50 border border-gray-100 hover:border-blue-100 transition-colors"
              title={t('common.view', 'View Details')}
            >
              <Eye className="w-3.5 h-3.5" />
            </Link>
            {!isTerminal ? (
              <button 
                onClick={() => openStatusModal(row)}
                className="p-1.5 rounded-xl text-gray-400 hover:text-indigo-600 hover:bg-indigo-50 border border-gray-100 hover:border-indigo-100 transition-colors"
                title={t('applications.updateStage', 'Update Status Workflow')}
              >
                <Edit2 className="w-3.5 h-3.5" />
              </button>
            ) : (
              <div className="w-7 h-7" />
            )}
            <button 
              onClick={() => handleDeleteClick(row)}
              className="p-1.5 rounded-xl text-gray-400 hover:text-red-600 hover:bg-red-50 border border-gray-100 hover:border-red-100 transition-colors"
              title={t('common.delete', 'Delete Application')}
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
          <h2 className="text-2xl font-bold text-gray-800 tracking-tight">{t('applications.title', 'Job Applications')}</h2>
          <p className="text-gray-500 mt-1">{t('applications.subtitle', 'Review applicant tracking lists and advance hiring pipeline stages.')}</p>
        </div>
        <Link 
          to="/applications/new"
          className="inline-flex items-center px-4 py-2.5 bg-blue-600 text-white font-semibold text-sm rounded-xl hover:bg-blue-700 transition-colors shadow-md shadow-blue-100 gap-1.5 self-start sm:self-auto"
        >
          <Plus className="w-4 h-4" /> {t('applications.addApplication', 'New Application')}
        </Link>
      </div>

      {/* Advanced filters */}
      <div className="bg-white border border-gray-200 rounded-2xl p-5 shadow-sm space-y-4">
        <div className="flex items-center gap-2 text-gray-800 border-b border-gray-100 pb-3">
          <SlidersHorizontal className="w-4 h-4 text-blue-500" />
          <h4 className="font-bold text-sm uppercase tracking-wider">{t('common.filter', 'Search & Filters')}</h4>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {/* Keyword Search */}
          <div className="relative">
            <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
            <input
              type="text"
              value={searchKeyword}
              onChange={(e) => { setSearchKeyword(e.target.value); setPage(0); }}
              placeholder={t('common.search', 'Search candidate name or job title...')}
              className="w-full pl-10 pr-4 py-2.5 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 text-xs font-semibold bg-gray-50/50"
            />
          </div>

          {/* Company Filter */}
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

          {/* Status Filter */}
          <div className="relative">
            <SlidersHorizontal className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
            <select
              value={statusFilter}
              onChange={(e) => { setStatusFilter(e.target.value); setPage(0); }}
              className="w-full pl-10 pr-4 py-2.5 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 text-xs font-semibold bg-white text-gray-600 appearance-none"
            >
              <option value="">{t('jobs.allStatuses', 'All Statuses')}</option>
              <option value="APPLIED">{t('status.APPLIED', 'Applied')}</option>
              <option value="REVIEWING">{t('status.REVIEWING', 'Reviewing')}</option>
              <option value="INTERVIEW">{t('status.INTERVIEW', 'Interview')}</option>
              <option value="OFFER">{t('status.OFFER', 'Offer')}</option>
              <option value="HIRED">{t('status.HIRED', 'Hired')}</option>
              <option value="REJECTED">{t('status.REJECTED', 'Rejected')}</option>
            </select>
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

      {/* Applications Table grid */}
      <DataTable 
        columns={columns} 
        data={apps} 
        loading={loading}
        emptyMessage={
          <div className="text-center py-8">
            <FileText className="w-12 h-12 text-gray-300 mx-auto" />
            <h3 className="font-bold text-gray-700 mt-4">{t('common.noData', 'No Job Applications')}</h3>
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

      {/* Soft-deletion Overlay Dialog */}
      <ConfirmDialog 
        open={deleteOpen}
        title={t('applications.deleteTitle', 'Delete Job Application')}
        message={t('applications.deleteConfirm', 'Are you sure you want to delete this job application?')}
        onConfirm={handleDeleteConfirm}
        onCancel={() => {
          setDeleteOpen(false);
          setAppToDelete(null);
        }}
        confirmText={t('common.delete', 'Delete')}
        loading={deleteLoading}
      />

      {/* Status update Modal Sheet overlay */}
      <StatusUpdateModal
        open={statusModalOpen}
        currentStatus={appForStatusUpdate?.applicationStatus}
        onConfirm={handleUpdateStatusConfirm}
        onCancel={() => {
          setStatusModalOpen(false);
          setAppForStatusUpdate(null);
        }}
        loading={statusLoading}
      />
    </div>
  );
};

export default Applications;
