import React, { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { toast } from 'react-hot-toast';
import { ArrowLeft, Edit2, MapPin, DollarSign, Calendar, Clock, Loader2, Briefcase, Users } from 'lucide-react';
import axiosClient from '../../services/axiosClient';
import StatusBadge from '../../components/ui/StatusBadge';

const JobDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { t } = useTranslation();
  const [loading, setLoading] = useState(true);
  const [job, setJob] = useState(null);

  useEffect(() => {
    const fetchJob = async () => {
      try {
        const response = await axiosClient.get(`/jobs/${id}`);
        setJob(response.data.data);
      } catch (err) {
        console.error('Error fetching job details:', err);
        toast.error(t('common.error', 'Failed to load job profile details.'));
        navigate('/jobs');
      } finally {
        setLoading(false);
      }
    };
    fetchJob();
  }, [id, navigate, t]);

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

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] gap-3 text-gray-450 font-semibold text-sm">
        <Loader2 className="w-8 h-8 animate-spin text-blue-500" />
        <span>{t('common.loading', 'Loading job details...')}</span>
      </div>
    );
  }

  if (!job) return null;

  return (
    <div className="max-w-4xl mx-auto space-y-6 animate-fadeIn">
      {/* Header controls */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex items-center gap-4">
          <Link 
            to="/jobs" 
            className="p-2 border border-gray-200 text-gray-500 hover:text-gray-800 rounded-xl hover:bg-gray-50 transition-colors bg-white shadow-sm"
          >
            <ArrowLeft className="w-4 h-4" />
          </Link>
          <div>
            <h2 className="text-2xl font-bold text-gray-800 tracking-tight">{job.title}</h2>
            <p className="text-xs text-gray-400 font-semibold uppercase tracking-wider">{t('jobs.jobDetails', 'Job Posting Details')}</p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <Link
            to={`/jobs/${id}/edit`}
            className="inline-flex items-center px-4 py-2.5 bg-blue-600 text-white font-semibold text-sm rounded-xl hover:bg-blue-700 transition-colors shadow-md shadow-blue-100 gap-1.5"
          >
            <Edit2 className="w-4 h-4" /> {t('jobs.editJob', 'Edit Job')}
          </Link>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 items-start">
        {/* Main description panel */}
        <div className="bg-white border border-gray-200 rounded-2xl p-6 md:p-8 shadow-sm lg:col-span-2 space-y-6">
          <div className="flex items-center justify-between border-b border-gray-100 pb-4">
            <div className="flex items-center gap-3">
              <div className="w-12 h-12 rounded-xl bg-blue-50 text-blue-700 flex items-center justify-center font-bold text-lg border border-blue-100">
                <Briefcase className="w-5 h-5" />
              </div>
              <div>
                <h3 className="font-bold text-gray-800 text-lg">{job.title}</h3>
                <Link to={`/companies/${job.company?.id}`} className="text-sm font-semibold text-blue-600 hover:underline">
                  {job.company?.companyName}
                </Link>
              </div>
            </div>
            <StatusBadge status={job.status} />
          </div>

          <div className="space-y-3">
            <h4 className="text-sm font-bold text-gray-400 uppercase tracking-wider">{t('jobs.description', 'Job Description')}</h4>
            <p className="text-sm text-gray-600 leading-relaxed font-medium whitespace-pre-line">
              {job.description || t('common.noData', 'No description provided for this job opening.')}
            </p>
          </div>

          <div className="space-y-3 pt-4 border-t border-gray-100">
            <h4 className="text-sm font-bold text-gray-400 uppercase tracking-wider">{t('jobs.requirements', 'Requirements & Qualifications')}</h4>
            <p className="text-sm text-gray-600 leading-relaxed font-medium whitespace-pre-line">
              {job.requirements || t('common.noData', 'No specific requirements listed.')}
            </p>
          </div>
        </div>

        {/* Metadata sidebar */}
        <div className="bg-white border border-gray-200 rounded-2xl p-6 shadow-sm space-y-6">
          <h3 className="font-bold text-gray-800 text-sm border-b border-gray-100 pb-3 uppercase tracking-wider">
            {t('jobs.title', 'Position Overview')}
          </h3>

          <div className="space-y-4 text-sm font-medium text-gray-600">
            <div className="flex items-center gap-3">
              <MapPin className="w-4 h-4 text-gray-400 flex-shrink-0" />
              <span>{job.location || 'Remote'}</span>
            </div>
            <div className="flex items-center gap-3">
              <Briefcase className="w-4 h-4 text-gray-400 flex-shrink-0" />
              <span className="capitalize">{formatEmploymentType(job.employmentType)}</span>
            </div>
            <div className="flex items-center gap-3">
              <Users className="w-4 h-4 text-gray-400 flex-shrink-0" />
              <span>{t('jobs.experienceLevel', 'Experience')}: <strong className="text-gray-800 font-semibold capitalize">{job.experienceLevel?.toLowerCase() || 'Mid'}</strong></span>
            </div>
            <div className="flex items-center gap-3">
              <DollarSign className="w-4 h-4 text-gray-400 flex-shrink-0" />
              <span>
                {job.salaryMin !== undefined && job.salaryMax !== undefined ? (
                  `${formatCurrency(job.salaryMin)} - ${formatCurrency(job.salaryMax)}`
                ) : (
                  'Not disclosed'
                )}
              </span>
            </div>
            <div className="flex items-center gap-3 pt-2 border-t border-gray-100 text-xs text-gray-400 font-semibold">
              <Calendar className="w-4 h-4 text-gray-400 flex-shrink-0" />
              <span>{t('jobs.deadline', 'Deadline')}: {formatDate(job.deadline)}</span>
            </div>
            <div className="flex items-center gap-3 text-xs text-gray-400 font-semibold">
              <Clock className="w-4 h-4 text-gray-400 flex-shrink-0" />
              <span>{t('applications.appliedDate', 'Posted')}: {formatDate(job.createdAt)}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default JobDetail;
