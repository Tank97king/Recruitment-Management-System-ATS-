import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { toast } from 'react-hot-toast';
import { Plus, Eye, Edit2, Trash2, ArrowUpDown, Briefcase, PenSquare } from 'lucide-react';
import axiosClient from '../../services/axiosClient';
import DataTable from '../../components/ui/DataTable';
import FilterPanel from './components/FilterPanel';
import Pagination from '../../components/ui/Pagination';
import ConfirmDialog from '../../components/ui/ConfirmDialog';
import StatusBadge from '../../components/ui/StatusBadge';

const Jobs = () => {
  const { t } = useTranslation();
  const [loading, setLoading] = useState(true);
  const [jobs, setJobs] = useState([]);
  
  // Filtering states
  const [filters, setFilters] = useState({
    keyword: '',
    companyId: '',
    status: '',
    employmentType: '',
    location: ''
  });

  // Paging states
  const [page, setPage] = useState(0);
  const [size] = useState(10);
  const [sortBy, setSortBy] = useState('createdAt');
  const [sortDirection, setSortDirection] = useState('desc');

  // Metadata
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  // Deletion overlay states
  const [deleteOpen, setDeleteOpen] = useState(false);
  const [jobToDelete, setJobToDelete] = useState(null);
  const [deleteLoading, setDeleteLoading] = useState(false);

  const fetchJobs = useCallback(async () => {
    setLoading(true);
    try {
      const response = await axiosClient.get('/jobs', {
        params: {
          keyword: filters.keyword || undefined,
          companyId: filters.companyId || undefined,
          status: filters.status || undefined,
          employmentType: filters.employmentType || undefined,
          location: filters.location || undefined,
          page,
          size,
          sortBy,
          sortDirection,
        },
      });
      const pageData = response.data.data;
      setJobs(pageData.content || []);
      setTotalPages(pageData.totalPages || 1);
      setTotalElements(pageData.totalElements || 0);
    } catch (err) {
      console.error('Error fetching jobs:', err);
      toast.error(t('common.error', 'Failed to retrieve job postings.'));
    } finally {
      setLoading(false);
    }
  }, [filters, page, size, sortBy, sortDirection, t]);

  useEffect(() => {
    fetchJobs();
  }, [fetchJobs]);

  const handleFilterChange = useCallback((updatedFilters) => {
    setFilters(updatedFilters);
    setPage(0);
  }, []);

  const handleSort = (field) => {
    if (sortBy === field) {
      setSortDirection(prev => (prev === 'asc' ? 'desc' : 'asc'));
    } else {
      setSortBy(field);
      setSortDirection('asc');
    }
    setPage(0);
  };

  const handleDeleteClick = (job) => {
    setJobToDelete(job);
    setDeleteOpen(true);
  };

  const handleDeleteConfirm = async () => {
    if (!jobToDelete) return;
    setDeleteLoading(true);
    try {
      await axiosClient.delete(`/jobs/${jobToDelete.id}`);
      toast.success(t('common.success', 'Job posting deleted successfully!'));
      setDeleteOpen(false);
      setJobToDelete(null);
      
      if (jobs.length === 1 && page > 0) {
        setPage(prev => prev - 1);
      } else {
        fetchJobs();
      }
    } catch (err) {
      console.error('Error deleting job posting:', err);
      const msg = err.response?.data?.message || t('common.error', 'Failed to delete job posting.');
      toast.error(msg);
    } finally {
      setDeleteLoading(false);
    }
  };

  const formatDate = (isoString) => {
    if (!isoString) return 'N/A';
    return new Date(isoString).toLocaleDateString();
  };

  const formatCurrency = (amount) => {
    if (amount === undefined || amount === null) return 'N/A';
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD', maximumFractionDigits: 0 }).format(amount);
  };

  const formatEmploymentType = (type) => {
    if (!type) return 'N/A';
    return type.replace('_', ' ').toLowerCase();
  };

  const columns = [
    {
      header: (
        <button 
          onClick={() => handleSort('title')} 
          className="flex items-center gap-1 hover:text-gray-700 transition-colors uppercase tracking-wider font-bold"
        >
          {t('jobs.jobTitle', 'Job Title')} <ArrowUpDown className="w-3.5 h-3.5" />
        </button>
      ),
      cell: (row) => (
        <div>
          <Link to={`/jobs/${row.id}`} className="font-bold text-gray-800 hover:text-blue-600 transition-colors">
            {row.title}
          </Link>
          <span className="block text-xs text-gray-400 font-semibold">
            {t('jobs.experienceLevel', 'Level')}: {row.experienceLevel?.toLowerCase() || 'Mid'}
          </span>
        </div>
      )
    },
    {
      header: t('jobs.company', 'Company'),
      cell: (row) => (
        <Link to={`/companies/${row.company?.id}`} className="font-semibold text-gray-600 hover:underline">
          {row.company?.companyName}
        </Link>
      )
    },
    {
      header: t('jobs.location', 'Location'),
      accessor: 'location',
      cell: (row) => <span className="text-gray-600 font-medium">{row.location || 'Remote'}</span>
    },
    {
      header: t('jobs.jobType', 'Employment Type'),
      cell: (row) => <span className="text-gray-600 font-medium capitalize">{formatEmploymentType(row.employmentType)}</span>
    },
    {
      header: t('jobs.salaryRange', 'Salary Range'),
      cell: (row) => (
        <span className="text-gray-600 font-medium">
          {row.salaryMin !== undefined && row.salaryMax !== undefined ? (
            `${formatCurrency(row.salaryMin)} - ${formatCurrency(row.salaryMax)}`
          ) : (
            'Not disclosed'
          )}
        </span>
      )
    },
    {
      header: t('jobs.status', 'Status'),
      cell: (row) => <StatusBadge status={row.status} />
    },
    {
      header: (
        <button 
          onClick={() => handleSort('deadline')} 
          className="flex items-center gap-1 hover:text-gray-700 transition-colors uppercase tracking-wider font-bold"
        >
          {t('jobs.deadline', 'Deadline')} <ArrowUpDown className="w-3.5 h-3.5" />
        </button>
      ),
      cell: (row) => <span className="text-gray-500 font-medium">{formatDate(row.deadline)}</span>
    },
    {
      header: (
        <button 
          onClick={() => handleSort('createdAt')} 
          className="flex items-center gap-1 hover:text-gray-700 transition-colors uppercase tracking-wider font-bold"
        >
          {t('applications.appliedDate', 'Created')} <ArrowUpDown className="w-3.5 h-3.5" />
        </button>
      ),
      cell: (row) => <span className="text-gray-500 font-medium">{formatDate(row.createdAt)}</span>
    },
    {
      header: t('common.actions', 'Actions'),
      cellClassName: 'text-right',
      cell: (row) => (
        <div className="flex items-center gap-1">
          <Link
            to={`/jobs/${row.id}`}
            className="p-2 rounded-xl text-gray-400 hover:text-blue-600 hover:bg-blue-50 border border-gray-200 hover:border-blue-200 transition-colors"
            title={t('common.view', 'View Job Details')}
          >
            <Eye className="w-4 h-4" />
          </Link>
          <Link
            to={`/jobs/${row.id}/edit`}
            className="p-2 rounded-xl text-gray-400 hover:text-indigo-600 hover:bg-indigo-50 border border-gray-200 hover:border-indigo-200 transition-colors"
            title={t('common.edit', 'Edit Job Posting')}
          >
            <PenSquare className="w-4 h-4" />
          </Link>
          <button
            onClick={() => handleDeleteClick(row)}
            className="p-2 rounded-xl text-gray-400 hover:text-red-600 hover:bg-red-50 border border-gray-200 hover:border-red-200 transition-colors"
            title={t('common.delete', 'Delete Job')}
          >
            <Trash2 className="w-4 h-4" />
          </button>
        </div>
      )
    }
  ];

  return (
    <div className="space-y-6 animate-fadeIn">
      {/* Header controls */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-gray-800 tracking-tight">{t('jobs.title', 'Job Postings')}</h2>
          <p className="text-gray-500 mt-1">{t('jobs.subtitle', 'Publish job openings, manage applications, and define requirements.')}</p>
        </div>
        <Link 
          to="/jobs/new"
          className="inline-flex items-center px-4 py-2.5 bg-blue-600 text-white font-semibold text-sm rounded-xl hover:bg-blue-700 transition-colors shadow-md shadow-blue-100 gap-1.5 self-start sm:self-auto"
        >
          <Plus className="w-4 h-4" /> {t('jobs.addJob', 'Post New Job')}
        </Link>
      </div>

      {/* Filter panel */}
      <FilterPanel onFilterChange={handleFilterChange} />

      {/* Jobs data table */}
      <DataTable 
        columns={columns} 
        data={jobs} 
        loading={loading}
        emptyMessage={
          <div className="text-center py-8">
            <Briefcase className="w-12 h-12 text-gray-300 mx-auto" />
            <h3 className="font-bold text-gray-700 mt-4">{t('common.noData', 'No Job Postings Found')}</h3>
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

      {/* Deletion Confirm Dialog */}
      <ConfirmDialog 
        open={deleteOpen}
        title={t('jobs.deleteTitle', 'Delete Job Posting')}
        message={t('jobs.deleteConfirm', `Are you sure you want to delete the job posting for '${jobToDelete?.title}'?`)}
        onConfirm={handleDeleteConfirm}
        onCancel={() => {
          setDeleteOpen(false);
          setJobToDelete(null);
        }}
        confirmText={t('common.delete', 'Delete')}
        loading={deleteLoading}
      />
    </div>
  );
};

export default Jobs;
