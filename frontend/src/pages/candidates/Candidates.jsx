import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { toast } from 'react-hot-toast';
import { Plus, Eye, Edit2, Trash2, ArrowUpDown, User, Search, SlidersHorizontal, RotateCcw, Upload, Download, FileText, Briefcase, X } from 'lucide-react';
import axiosClient from '../../services/axiosClient';
import DataTable from '../../components/ui/DataTable';
import Pagination from '../../components/ui/Pagination';
import ConfirmDialog from '../../components/ui/ConfirmDialog';
import SkillBadge from '../../components/ui/SkillBadge';
import CvUpload from './components/CvUpload';

const Candidates = () => {
  const { t } = useTranslation();
  const [loading, setLoading] = useState(true);
  const [candidates, setCandidates] = useState([]);
  
  // Search & Filter state
  const [search, setSearch] = useState('');
  const [expFilter, setExpFilter] = useState('');
  const [hasCvFilter, setHasCvFilter] = useState('');
  const [skillsFilter, setSkillsFilter] = useState('');

  // Pagination & Sorting state
  const [page, setPage] = useState(0);
  const [size] = useState(10);
  const [sortBy, setSortBy] = useState('fullName');
  const [sortDirection, setSortDirection] = useState('asc');

  // Metadata
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  // Modals state
  const [deleteOpen, setDeleteOpen] = useState(false);
  const [candidateToDelete, setCandidateToDelete] = useState(null);
  const [deleteLoading, setDeleteLoading] = useState(false);

  // Quick CV upload overlay modal state
  const [uploadModalOpen, setUploadModalOpen] = useState(false);
  const [candidateForUpload, setCandidateForUpload] = useState(null);

  // Download state
  const [downloadingId, setDownloadingId] = useState(null);

  const fetchCandidates = useCallback(async () => {
    setLoading(true);
    try {
      const response = await axiosClient.get('/candidates', {
        params: {
          search: search || undefined,
          page,
          size,
          sortBy,
          sortDirection,
        },
      });
      const pageData = response.data.data;
      setCandidates(pageData.content || []);
      setTotalPages(pageData.totalPages || 1);
      setTotalElements(pageData.totalElements || 0);
    } catch (err) {
      console.error('Error fetching candidates:', err);
      toast.error(t('common.error', 'Failed to load candidate profiles.'));
    } finally {
      setLoading(false);
    }
  }, [search, page, size, sortBy, sortDirection, t]);

  useEffect(() => {
    fetchCandidates();
  }, [fetchCandidates]);

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
    setSearch('');
    setExpFilter('');
    setHasCvFilter('');
    setSkillsFilter('');
    setPage(0);
  };

  const handleDeleteClick = (candidate) => {
    setCandidateToDelete(candidate);
    setDeleteOpen(true);
  };

  const handleDeleteConfirm = async () => {
    if (!candidateToDelete) return;
    setDeleteLoading(true);
    try {
      await axiosClient.delete(`/candidates/${candidateToDelete.id}`);
      toast.success(t('common.success', 'Candidate deleted successfully!'));
      setDeleteOpen(false);
      setCandidateToDelete(null);
      
      if (candidates.length === 1 && page > 0) {
        setPage(prev => prev - 1);
      } else {
        fetchCandidates();
      }
    } catch (err) {
      console.error('Error deleting candidate:', err);
      const msg = err.response?.data?.message || t('common.error', 'Failed to delete candidate profile.');
      toast.error(msg);
    } finally {
      setDeleteLoading(false);
    }
  };

  const handleDownloadCv = async (candidate) => {
    setDownloadingId(candidate.id);
    try {
      const response = await axiosClient.get(`/candidates/${candidate.id}/cv`, {
        responseType: 'blob',
      });
      const blob = new Blob([response.data], { type: 'application/pdf' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', candidate.cvFileName || `${candidate.fullName.replace(' ', '_')}_resume.pdf`);
      document.body.appendChild(link);
      link.click();
      link.parentNode.removeChild(link);
      window.URL.revokeObjectURL(url);
    } catch (err) {
      console.error('Error downloading CV:', err);
      toast.error(t('common.error', 'Failed to download CV file.'));
    } finally {
      setDownloadingId(null);
    }
  };

  const openUploadModal = (candidate) => {
    setCandidateForUpload(candidate);
    setUploadModalOpen(true);
  };

  const handleUploadSuccess = (updatedCandidate) => {
    setCandidates(prev => prev.map(c => c.id === updatedCandidate.id ? updatedCandidate : c));
    setUploadModalOpen(false);
    setCandidateForUpload(null);
  };

  const formatDate = (isoString) => {
    if (!isoString) return 'N/A';
    return new Date(isoString).toLocaleDateString();
  };

  // Client-side filtration over candidates retrieved from backend page response
  const filteredCandidates = candidates.filter(candidate => {
    // Years of Experience filter
    if (expFilter !== '' && candidate.yearsOfExperience !== parseInt(expFilter)) {
      return false;
    }
    // CV presence filter
    if (hasCvFilter !== '' && candidate.hasCv !== (hasCvFilter === 'true')) {
      return false;
    }
    // Skill tags filter
    if (skillsFilter.trim() !== '') {
      const querySkill = skillsFilter.toLowerCase().trim();
      const hasSkillMatch = candidate.skills?.some(s => s.toLowerCase().includes(querySkill));
      if (!hasSkillMatch) return false;
    }
    return true;
  });

  const columns = [
    {
      header: (
        <button 
          onClick={() => handleSort('fullName')} 
          className="flex items-center gap-1 hover:text-gray-700 transition-colors uppercase tracking-wider font-bold"
        >
          {t('candidates.name', 'Full Name')} <ArrowUpDown className="w-3.5 h-3.5" />
        </button>
      ),
      cell: (row) => (
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-xl bg-blue-50 text-blue-700 flex items-center justify-center font-bold text-sm border border-blue-100 uppercase flex-shrink-0">
            {row.fullName.charAt(0)}
          </div>
          <div>
            <Link to={`/candidates/${row.id}`} className="font-bold text-gray-800 hover:text-blue-600 transition-colors">
              {row.fullName}
            </Link>
            {row.currentTitle && (
              <span className="block text-xs text-gray-400 font-semibold truncate max-w-[150px]">
                {row.currentTitle}
              </span>
            )}
          </div>
        </div>
      )
    },
    {
      header: t('candidates.email', 'Email'),
      cell: (row) => <span className="font-semibold text-gray-600">{row.email}</span>
    },
    {
      header: t('candidates.phone', 'Phone'),
      cell: (row) => <span className="text-gray-600 font-medium">{row.phone || 'N/A'}</span>
    },
    {
      header: (
        <button 
          onClick={() => handleSort('yearsOfExperience')} 
          className="flex items-center gap-1 hover:text-gray-700 transition-colors uppercase tracking-wider font-bold"
        >
          {t('candidates.experienceYears', 'Experience')} <ArrowUpDown className="w-3.5 h-3.5" />
        </button>
      ),
      cellClassName: 'text-center',
      cell: (row) => (
        <span className="text-gray-600 font-bold text-xs">
          {row.yearsOfExperience !== undefined ? `${row.yearsOfExperience} yrs` : 'N/A'}
        </span>
      )
    },
    {
      header: t('candidates.education', 'Education'),
      cell: (row) => <span className="text-gray-600 text-xs font-semibold line-clamp-1 max-w-[150px]">{row.highestEducation || 'N/A'}</span>
    },
    {
      header: t('candidates.skills', 'Technical Skills'),
      cell: (row) => (
        <div className="flex flex-wrap gap-1 max-w-[200px]">
          {row.skills && row.skills.slice(0, 2).map((skill, idx) => (
            <SkillBadge key={idx} skill={skill} className="text-[10px]" />
          ))}
          {row.skills && row.skills.length > 2 && (
            <span className="text-[10px] text-gray-400 font-bold self-center">+{row.skills.length - 2} more</span>
          )}
          {(!row.skills || row.skills.length === 0) && <span className="text-xs text-gray-400 font-semibold">N/A</span>}
        </div>
      )
    },
    {
      header: t('candidates.cvDocument', 'CV Status'),
      cell: (row) => (
        <span className={`inline-flex px-2.5 py-0.5 rounded-full text-[10px] font-bold border ${
          row.hasCv 
            ? 'bg-emerald-50 text-emerald-700 border-emerald-100' 
            : 'bg-amber-50 text-amber-700 border-amber-100'
        }`}>
          {row.hasCv ? t('common.success', 'Uploaded') : t('candidates.noCv', 'Missing')}
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
      header: t('common.actions', 'Actions'),
      cellClassName: 'text-right',
      cell: (row) => (
        <div className="flex items-center justify-end gap-1.5">
          <Link 
            to={`/candidates/${row.id}`} 
            className="p-1.5 rounded-xl text-gray-400 hover:text-blue-600 hover:bg-blue-50 border border-gray-100 hover:border-blue-100 transition-colors"
            title={t('common.view', 'View Details')}
          >
            <Eye className="w-3.5 h-3.5" />
          </Link>
          <Link 
            to={`/candidates/${row.id}/edit`} 
            className="p-1.5 rounded-xl text-gray-400 hover:text-indigo-600 hover:bg-indigo-50 border border-gray-100 hover:border-indigo-100 transition-colors"
            title={t('common.edit', 'Edit Profile')}
          >
            <Edit2 className="w-3.5 h-3.5" />
          </Link>
          <button 
            onClick={() => openUploadModal(row)}
            className="p-1.5 rounded-xl text-gray-400 hover:text-amber-600 hover:bg-amber-50 border border-gray-100 hover:border-amber-100 transition-colors"
            title={t('candidates.uploadCv', 'Upload PDF CV')}
          >
            <Upload className="w-3.5 h-3.5" />
          </button>
          {row.hasCv ? (
            <button 
              onClick={() => handleDownloadCv(row)}
              disabled={downloadingId === row.id}
              className="p-1.5 rounded-xl text-gray-400 hover:text-emerald-600 hover:bg-emerald-50 border border-gray-100 hover:border-emerald-100 transition-colors disabled:opacity-50"
              title={t('candidates.downloadCv', 'Download CV')}
            >
              <Download className="w-3.5 h-3.5" />
            </button>
          ) : (
            <div className="w-8" />
          )}
          <button 
            onClick={() => handleDeleteClick(row)}
            className="p-1.5 rounded-xl text-gray-400 hover:text-red-600 hover:bg-red-50 border border-gray-100 hover:border-red-100 transition-colors"
            title={t('common.delete', 'Delete Candidate')}
          >
            <Trash2 className="w-3.5 h-3.5" />
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
          <h2 className="text-2xl font-bold text-gray-800 tracking-tight">{t('candidates.title', 'Candidates')}</h2>
          <p className="text-gray-500 mt-1">{t('candidates.subtitle', 'Manage candidate profiles, review technical skills, and manage CVs.')}</p>
        </div>
        <Link 
          to="/candidates/new"
          className="inline-flex items-center px-4 py-2.5 bg-blue-600 text-white font-semibold text-sm rounded-xl hover:bg-blue-700 transition-colors shadow-md shadow-blue-100 gap-1.5 self-start sm:self-auto"
        >
          <Plus className="w-4 h-4" /> {t('candidates.addCandidate', 'Add Candidate')}
        </Link>
      </div>

      {/* Advanced Filter Panel */}
      <div className="bg-white border border-gray-200 rounded-2xl p-5 shadow-sm space-y-4">
        <div className="flex items-center gap-2 text-gray-800 border-b border-gray-100 pb-3">
          <SlidersHorizontal className="w-4 h-4 text-blue-500" />
          <h4 className="font-bold text-sm uppercase tracking-wider">{t('common.filter', 'Search & Filters')}</h4>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {/* Keyword Search */}
          <div className="relative">
            <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
            <input
              type="text"
              value={search}
              onChange={(e) => { setSearch(e.target.value); setPage(0); }}
              placeholder={t('common.search', 'Search name, email, phone...')}
              className="w-full pl-10 pr-4 py-2.5 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 text-xs font-semibold bg-gray-50/50"
            />
          </div>

          {/* Skill Filter */}
          <div className="relative">
            <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
            <input
              type="text"
              value={skillsFilter}
              onChange={(e) => { setSkillsFilter(e.target.value); setPage(0); }}
              placeholder={t('candidates.skills', 'Filter by skill keyword...')}
              className="w-full pl-10 pr-4 py-2.5 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 text-xs font-semibold bg-gray-50/50"
            />
          </div>

          {/* Experience Filter */}
          <div className="relative">
            <Briefcase className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
            <input
              type="number"
              value={expFilter}
              onChange={(e) => { setExpFilter(e.target.value); setPage(0); }}
              placeholder={t('candidates.experienceYears', 'Filter by exact years exp...')}
              className="w-full pl-10 pr-4 py-2.5 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 text-xs font-semibold bg-gray-50/50"
            />
          </div>

          {/* Has CV status Filter */}
          <div className="relative">
            <FileText className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
            <select
              value={hasCvFilter}
              onChange={(e) => { setHasCvFilter(e.target.value); setPage(0); }}
              className="w-full pl-10 pr-4 py-2.5 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 text-xs font-semibold bg-white text-gray-600 appearance-none"
            >
              <option value="">{t('candidates.cvDocument', 'All CV Statuses')}</option>
              <option value="true">{t('common.success', 'CV Uploaded')}</option>
              <option value="false">{t('candidates.noCv', 'CV Missing')}</option>
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

      {/* Candidates table */}
      <DataTable 
        columns={columns} 
        data={filteredCandidates} 
        loading={loading}
        emptyMessage={
          <div className="text-center py-8">
            <User className="w-12 h-12 text-gray-300 mx-auto" />
            <h3 className="font-bold text-gray-700 mt-4">{t('common.noData', 'No Candidates Found')}</h3>
          </div>
        }
      />

      {/* Pagination component */}
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
        title={t('candidates.deleteTitle', 'Delete Candidate profile')}
        message={t('candidates.deleteConfirm', `Are you sure you want to delete candidate '${candidateToDelete?.fullName}'?`)}
        onConfirm={handleDeleteConfirm}
        onCancel={() => {
          setDeleteOpen(false);
          setCandidateToDelete(null);
        }}
        confirmText={t('common.delete', 'Delete')}
        loading={deleteLoading}
      />

      {/* Quick CV Upload Modal overlay */}
      {uploadModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/55 animate-fadeIn">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-xl relative border border-gray-100">
            <button 
              onClick={() => { setUploadModalOpen(false); setCandidateForUpload(null); }}
              className="absolute right-4 top-4 p-1 text-gray-400 hover:text-gray-700 rounded-lg hover:bg-gray-50"
            >
              <X className="w-5 h-5" />
            </button>
            <h3 className="font-bold text-gray-800 text-base mb-2">{t('candidates.uploadCv', 'Upload CV for Candidate')}</h3>
            <p className="text-xs text-gray-500 font-semibold mb-4">{t('candidates.name', 'Candidate')}: {candidateForUpload?.fullName}</p>
            
            <CvUpload 
              candidateId={candidateForUpload?.id} 
              onUploadSuccess={handleUploadSuccess} 
            />
          </div>
        </div>
      )}
    </div>
  );
};

export default Candidates;
