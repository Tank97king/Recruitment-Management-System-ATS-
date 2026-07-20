import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { toast } from 'react-hot-toast';
import { Plus, Eye, Edit2, Trash2, ArrowUpDown, Building2 } from 'lucide-react';
import axiosClient from '../../services/axiosClient';
import DataTable from '../../components/ui/DataTable';
import SearchBar from '../../components/ui/SearchBar';
import Pagination from '../../components/ui/Pagination';
import ConfirmDialog from '../../components/ui/ConfirmDialog';

const Companies = () => {
  const { t } = useTranslation();
  const [loading, setLoading] = useState(true);
  const [companies, setCompanies] = useState([]);
  
  // Search, Paging, and Sort settings
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const [size] = useState(10);
  const [sortBy, setSortBy] = useState('companyName');
  const [sortDirection, setSortDirection] = useState('asc');
  
  // Metadata fields
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  // Deletion overlay states
  const [deleteOpen, setDeleteOpen] = useState(false);
  const [companyToDelete, setCompanyToDelete] = useState(null);
  const [deleteLoading, setDeleteLoading] = useState(false);

  const fetchCompanies = useCallback(async () => {
    setLoading(true);
    try {
      const response = await axiosClient.get('/companies', {
        params: {
          search: search || undefined,
          page,
          size,
          sortBy,
          sortDirection,
        },
      });
      const pageData = response.data.data;
      setCompanies(pageData.content || []);
      setTotalPages(pageData.totalPages || 1);
      setTotalElements(pageData.totalElements || 0);
    } catch (err) {
      console.error('Error loading companies:', err);
      toast.error(t('common.error', 'Failed to retrieve companies.'));
    } finally {
      setLoading(false);
    }
  }, [search, page, size, sortBy, sortDirection, t]);

  useEffect(() => {
    fetchCompanies();
  }, [fetchCompanies]);

  const handleSearchChange = (query) => {
    setSearch(query);
    setPage(0);
  };

  const handleSort = (field) => {
    if (sortBy === field) {
      setSortDirection(prev => (prev === 'asc' ? 'desc' : 'asc'));
    } else {
      setSortBy(field);
      setSortDirection('asc');
    }
    setPage(0);
  };

  const handleDeleteClick = (company) => {
    setCompanyToDelete(company);
    setDeleteOpen(true);
  };

  const handleDeleteConfirm = async () => {
    if (!companyToDelete) return;
    setDeleteLoading(true);
    try {
      await axiosClient.delete(`/companies/${companyToDelete.id}`);
      toast.success(t('common.success', 'Company deleted successfully!'));
      setDeleteOpen(false);
      setCompanyToDelete(null);
      
      if (companies.length === 1 && page > 0) {
        setPage(prev => prev - 1);
      } else {
        fetchCompanies();
      }
    } catch (err) {
      console.error('Error deleting company:', err);
      const status = err.response?.status;
      const msg = err.response?.data?.message || t('common.error', 'Failed to delete company.');
      
      if (status === 409) {
        toast.error(t('companies.cannotDeleteWarning', 'Cannot delete company with active open job postings. Please close all postings first.'));
      } else {
        toast.error(msg);
      }
    } finally {
      setDeleteLoading(false);
    }
  };

  const formatDate = (isoString) => {
    if (!isoString) return 'N/A';
    return new Date(isoString).toLocaleDateString();
  };

  const columns = [
    {
      header: (
        <button 
          onClick={() => handleSort('companyName')} 
          className="flex items-center gap-1 hover:text-gray-700 transition-colors uppercase tracking-wider font-bold"
        >
          {t('companies.companyName', 'Company Name')} <ArrowUpDown className="w-3.5 h-3.5" />
        </button>
      ),
      cell: (row) => (
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-xl bg-blue-50 text-blue-700 flex items-center justify-center font-bold text-sm border border-blue-100 flex-shrink-0">
            {row.companyName.charAt(0).toUpperCase()}
          </div>
          <div>
            <Link to={`/companies/${row.id}`} className="font-bold text-gray-800 hover:text-blue-600 transition-colors">
              {row.companyName}
            </Link>
            {row.website && (
              <span className="block text-xs text-gray-400 font-semibold truncate max-w-[180px]">
                {row.website.replace('https://', '').replace('http://', '')}
              </span>
            )}
          </div>
        </div>
      )
    },
    {
      header: 'Email',
      accessor: 'email',
      cell: (row) => row.email ? <span className="font-medium text-gray-600">{row.email}</span> : <span className="text-gray-400">N/A</span>
    },
    {
      header: t('candidates.phone', 'Phone'),
      accessor: 'phone',
      cell: (row) => row.phone ? <span className="text-gray-600">{row.phone}</span> : <span className="text-gray-400">N/A</span>
    },
    {
      header: t('companies.location', 'Address'),
      accessor: 'address',
      cell: (row) => row.address ? <span className="text-gray-600 line-clamp-1 max-w-[150px]">{row.address}</span> : <span className="text-gray-400">N/A</span>
    },
    {
      header: t('companies.activeJobsCount', 'Total Jobs'),
      accessor: 'totalJobs',
      cellClassName: 'text-center',
      cell: (row) => (
        <span className={`px-2.5 py-0.5 rounded-full text-xs font-bold ${
          row.totalJobs > 0 ? 'bg-blue-50 text-blue-700' : 'bg-gray-100 text-gray-400'
        }`}>
          {row.totalJobs}
        </span>
      )
    },
    {
      header: (
        <button 
          onClick={() => handleSort('createdAt')} 
          className="flex items-center gap-1 hover:text-gray-700 transition-colors uppercase tracking-wider font-bold"
        >
          {t('applications.appliedDate', 'Created Date')} <ArrowUpDown className="w-3.5 h-3.5" />
        </button>
      ),
      cell: (row) => <span className="text-gray-500 font-medium">{formatDate(row.createdAt)}</span>
    },
    {
      header: t('common.status', 'Status'),
      cell: () => (
        <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-100">
          {t('status.active', 'Active')}
        </span>
      )
    },
    {
      header: t('common.actions', 'Actions'),
      cellClassName: 'text-right',
      cell: (row) => (
        <div className="flex items-center justify-end gap-2">
          <Link 
            to={`/companies/${row.id}`} 
            className="p-2 rounded-xl text-gray-400 hover:text-blue-600 hover:bg-blue-50 border border-gray-100 hover:border-blue-100 transition-colors"
            title={t('common.view', 'View Details')}
          >
            <Eye className="w-4 h-4" />
          </Link>
          <Link 
            to={`/companies/${row.id}/edit`} 
            className="p-2 rounded-xl text-gray-400 hover:text-indigo-600 hover:bg-indigo-50 border border-gray-100 hover:border-indigo-100 transition-colors"
            title={t('common.edit', 'Edit Profile')}
          >
            <Edit2 className="w-4 h-4" />
          </Link>
          <button 
            onClick={() => handleDeleteClick(row)}
            className="p-2 rounded-xl text-gray-400 hover:text-red-600 hover:bg-red-50 border border-gray-100 hover:border-red-100 transition-colors"
            title={t('common.delete', 'Delete Company')}
          >
            <Trash2 className="w-4 h-4" />
          </button>
        </div>
      )
    }
  ];

  return (
    <div className="space-y-6 animate-fadeIn">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-gray-800 tracking-tight">{t('companies.title', 'Partner Companies')}</h2>
          <p className="text-gray-500 mt-1">{t('companies.subtitle', 'Manage and track company profiles, job postings count, and site details.')}</p>
        </div>
        <Link 
          to="/companies/new"
          className="inline-flex items-center px-4 py-2.5 bg-blue-600 text-white font-semibold text-sm rounded-xl hover:bg-blue-700 transition-colors shadow-md shadow-blue-100 gap-1.5 self-start sm:self-auto"
        >
          <Plus className="w-4 h-4" /> {t('companies.addCompany', 'Add Company')}
        </Link>
      </div>

      <div className="bg-white border border-gray-200 rounded-2xl p-4 shadow-sm">
        <SearchBar 
          value={search} 
          onChange={handleSearchChange} 
          placeholder={t('common.search', 'Search companies by name or email address...')} 
        />
      </div>

      <DataTable 
        columns={columns} 
        data={companies} 
        loading={loading}
        emptyMessage={
          <div className="text-center py-8">
            <Building2 className="w-12 h-12 text-gray-300 mx-auto" />
            <h3 className="font-bold text-gray-700 mt-4">{t('common.noData', 'No Companies Found')}</h3>
          </div>
        }
      />

      <Pagination 
        page={page} 
        totalPages={totalPages} 
        totalElements={totalElements} 
        size={size} 
        onPageChange={setPage} 
      />

      <ConfirmDialog 
        open={deleteOpen}
        title={t('companies.deleteTitle', 'Delete Company Profile')}
        message={t('companies.deleteConfirm', `Are you sure you want to delete the company profile for '${companyToDelete?.companyName}'?`)}
        onConfirm={handleDeleteConfirm}
        onCancel={() => {
          setDeleteOpen(false);
          setCompanyToDelete(null);
        }}
        confirmText={t('common.delete', 'Delete')}
        loading={deleteLoading}
      />
    </div>
  );
};

export default Companies;
