import React, { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { toast } from 'react-hot-toast';
import { ArrowLeft, Calendar, Clock, Loader2, CheckCircle2, ChevronRight } from 'lucide-react';
import axiosClient from '../../services/axiosClient';
import StatusBadge from '../../components/ui/StatusBadge';
import StatusUpdateModal from './components/StatusUpdateModal';

const ApplicationDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { t } = useTranslation();
  const [loading, setLoading] = useState(true);
  const [app, setApp] = useState(null);

  const [statusModalOpen, setStatusModalOpen] = useState(false);
  const [statusLoading, setStatusLoading] = useState(false);

  const fetchApplication = async () => {
    try {
      const response = await axiosClient.get(`/job-applications/${id}`);
      setApp(response.data.data);
    } catch (err) {
      console.error('Error fetching job application:', err);
      toast.error(t('common.error', 'Failed to load job application details.'));
      navigate('/applications');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchApplication();
  }, [id]);

  const handleUpdateStatusConfirm = async (nextStatus) => {
    setStatusLoading(true);
    try {
      const response = await axiosClient.put(`/job-applications/${id}/status`, {
        status: nextStatus
      });
      toast.success(t('common.success', 'Application status updated successfully!'));
      setApp(response.data.data);
      setStatusModalOpen(false);
    } catch (err) {
      console.error('Error updating status:', err);
      const msg = err.response?.data?.message || t('common.error', 'Failed to advance status.');
      toast.error(msg);
    } finally {
      setStatusLoading(false);
    }
  };

  const formatDate = (isoString) => {
    if (!isoString) return 'N/A';
    return new Date(isoString).toLocaleDateString();
  };

  const getTimelineSteps = (current) => {
    const isRejected = current === 'REJECTED';
    const standardStages = ['APPLIED', 'REVIEWING', 'INTERVIEW', 'OFFER', 'HIRED'];
    
    if (isRejected) {
      return [
        { label: t('status.APPLIED', 'Applied'), status: 'APPLIED', desc: 'Application received' },
        { label: t('status.REJECTED', 'Rejected'), status: 'REJECTED', desc: 'Process terminated', error: true }
      ];
    }

    const currentIndex = standardStages.indexOf(current);
    return [
      { label: t('status.APPLIED', 'Applied'), status: 'APPLIED', desc: 'Resume submitted successfully' },
      { label: t('status.REVIEWING', 'Under Review'), status: 'REVIEWING', desc: 'HR screening qualifications' },
      { label: t('status.INTERVIEW', 'Interviewing'), status: 'INTERVIEW', desc: 'Technical & cultural reviews' },
      { label: t('status.OFFER', 'Job Offer'), status: 'OFFER', desc: 'Compensation details extended' },
      { label: t('status.HIRED', 'Hired'), status: 'HIRED', desc: 'Contract finalized and signed' },
    ].map((step, idx) => {
      let state = 'upcoming';
      if (idx < currentIndex) {
        state = 'completed';
      } else if (idx === currentIndex) {
        state = 'active';
      }
      return { ...step, state };
    });
  };

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] gap-3 text-gray-400 font-semibold text-sm">
        <Loader2 className="w-8 h-8 animate-spin text-blue-500" />
        <span>{t('common.loading', 'Loading application details...')}</span>
      </div>
    );
  }

  if (!app) return null;

  const isTerminal = ['HIRED', 'REJECTED', 'WITHDRAWN'].includes(app.applicationStatus);
  const steps = getTimelineSteps(app.applicationStatus);

  return (
    <div className="max-w-4xl mx-auto space-y-6 animate-fadeIn">
      {/* Header controls */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex items-center gap-4">
          <Link 
            to="/applications" 
            className="p-2 border border-gray-200 text-gray-500 hover:text-gray-800 rounded-xl hover:bg-gray-50 transition-colors bg-white shadow-sm"
          >
            <ArrowLeft className="w-4 h-4" />
          </Link>
          <div>
            <h2 className="text-2xl font-bold text-gray-800 tracking-tight">{app.candidateName}</h2>
            <p className="text-xs text-gray-400 font-semibold uppercase tracking-wider">{t('applications.applicationDetails', 'Application Details')}</p>
          </div>
        </div>
        {!isTerminal && (
          <button
            onClick={() => setStatusModalOpen(true)}
            className="inline-flex items-center px-4 py-2.5 bg-blue-600 text-white font-semibold text-sm rounded-xl hover:bg-blue-700 transition-colors shadow-md shadow-blue-100 gap-1.5 self-start sm:self-auto"
          >
            {t('applications.updateStage', 'Advance Stage')} <ChevronRight className="w-4 h-4" />
          </button>
        )}
      </div>

      {/* Overview Card */}
      <div className="bg-white border border-gray-200 rounded-2xl p-6 md:p-8 shadow-sm space-y-6">
        <div className="flex flex-col md:flex-row md:items-center justify-between border-b border-gray-100 pb-5 gap-4">
          <div>
            <span className="text-xs text-gray-400 font-bold uppercase tracking-wider block mb-1">{t('applications.job', 'Target Opening')}</span>
            <Link to={`/jobs/${app.jobId}`} className="text-xl font-bold text-gray-800 hover:text-blue-600 transition-colors">
              {app.jobTitle}
            </Link>
            <p className="text-sm font-semibold text-gray-500 mt-0.5">{app.companyName}</p>
          </div>
          <div className="flex flex-col items-start md:items-end gap-1">
            <span className="text-xs text-gray-400 font-bold uppercase tracking-wider">{t('applications.currentStage', 'Current Status')}</span>
            <StatusBadge status={app.applicationStatus} />
          </div>
        </div>

        {/* Linear Stage Timeline Tracker */}
        <div className="space-y-3 pt-2">
          <h4 className="text-xs font-bold text-gray-400 uppercase tracking-wider">{t('pipeline.title', 'Stage Timeline')}</h4>
          <div className="grid grid-cols-1 sm:grid-cols-5 gap-3">
            {steps.map((step, idx) => (
              <div 
                key={idx} 
                className={`p-3 rounded-xl border flex flex-col justify-between ${
                  step.error 
                    ? 'bg-red-50 border-red-200 text-red-700' 
                    : step.state === 'completed'
                      ? 'bg-emerald-50/50 border-emerald-200 text-emerald-800'
                      : step.state === 'active'
                        ? 'bg-blue-50 border-blue-300 text-blue-800 ring-2 ring-blue-500/20'
                        : 'bg-gray-50/50 border-gray-100 text-gray-400'
                }`}
              >
                <div className="flex items-center justify-between mb-2">
                  <span className="text-xs font-bold">{step.label}</span>
                  {step.state === 'completed' && <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" />}
                </div>
                <span className="text-[10px] opacity-80 leading-tight">{step.desc}</span>
              </div>
            ))}
          </div>
        </div>

        {/* Additional info */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pt-4 border-t border-gray-100 text-sm font-medium text-gray-600">
          <div className="flex items-center gap-3">
            <Calendar className="w-4 h-4 text-gray-400 flex-shrink-0" />
            <span>{t('applications.appliedDate', 'Applied Date')}: <strong>{formatDate(app.appliedAt)}</strong></span>
          </div>
          <div className="flex items-center gap-3">
            <Clock className="w-4 h-4 text-gray-400 flex-shrink-0" />
            <span>{t('applications.appliedDate', 'Last Updated')}: <strong>{formatDate(app.updatedAt)}</strong></span>
          </div>
        </div>

        {app.coverLetter && (
          <div className="space-y-2 pt-4 border-t border-gray-100">
            <h4 className="text-sm font-bold text-gray-400 uppercase tracking-wider">{t('applications.notes', 'Cover Letter')}</h4>
            <p className="text-sm text-gray-600 leading-relaxed whitespace-pre-line bg-gray-50/50 p-4 rounded-xl border border-gray-100">
              {app.coverLetter}
            </p>
          </div>
        )}
      </div>

      {/* Status Modal */}
      <StatusUpdateModal
        open={statusModalOpen}
        currentStatus={app.applicationStatus}
        onConfirm={handleUpdateStatusConfirm}
        onCancel={() => setStatusModalOpen(false)}
        loading={statusLoading}
      />
    </div>
  );
};

export default ApplicationDetail;
