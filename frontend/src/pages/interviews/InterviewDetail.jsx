import React, { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { toast } from 'react-hot-toast';
import { ArrowLeft, Edit2, Calendar, Clock, Loader2, User, Mail, Video, MapPin, Briefcase, CheckCircle2, AlertTriangle } from 'lucide-react';
import axiosClient from '../../services/axiosClient';
import StatusBadge from '../../components/ui/StatusBadge';
import InterviewStatusModal from './components/InterviewStatusModal';

const InterviewDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { t } = useTranslation();
  const [loading, setLoading] = useState(true);
  const [interview, setInterview] = useState(null);

  const [statusModalOpen, setStatusModalOpen] = useState(false);
  const [statusLoading, setStatusLoading] = useState(false);

  const fetchInterview = async () => {
    try {
      const response = await axiosClient.get(`/interviews/${id}`);
      setInterview(response.data.data);
    } catch (err) {
      console.error('Error fetching interview details:', err);
      toast.error(t('common.error', 'Failed to load interview details.'));
      navigate('/interviews');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchInterview();
  }, [id]);

  const handleUpdateStatusConfirm = async (nextStatus) => {
    setStatusLoading(true);
    try {
      const response = await axiosClient.patch(`/interviews/${id}/status`, {
        status: nextStatus
      });
      toast.success(t('common.success', 'Interview status updated successfully!'));
      setInterview(response.data.data);
      setStatusModalOpen(false);
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

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] gap-3 text-gray-400 font-semibold text-sm">
        <Loader2 className="w-8 h-8 animate-spin text-blue-500" />
        <span>{t('common.loading', 'Fetching interview parameters...')}</span>
      </div>
    );
  }

  if (!interview) return null;

  const isOnline = interview.interviewType === 'ONLINE' || interview.interviewType === 'VIDEO_CALL';

  return (
    <div className="max-w-4xl mx-auto space-y-6 animate-fadeIn">
      {/* Header controls */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex items-center gap-4">
          <Link 
            to="/interviews" 
            className="p-2 border border-gray-200 text-gray-500 hover:text-gray-800 rounded-xl hover:bg-gray-50 transition-colors bg-white shadow-sm"
          >
            <ArrowLeft className="w-4 h-4" />
          </Link>
          <div>
            <h2 className="text-2xl font-bold text-gray-800 tracking-tight">{interview.candidateName}</h2>
            <p className="text-xs text-gray-400 font-semibold uppercase tracking-wider">{t('interviews.interviewDetails', 'Interview Session Details')}</p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <Link
            to={`/interviews/${id}/edit`}
            className="inline-flex items-center px-4 py-2.5 bg-blue-600 text-white font-semibold text-sm rounded-xl hover:bg-blue-700 transition-colors shadow-md shadow-blue-100 gap-1.5"
          >
            <Edit2 className="w-4 h-4" /> {t('interviews.editInterview', 'Edit Session')}
          </Link>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 items-start">
        {/* Main Details Panel */}
        <div className="bg-white border border-gray-200 rounded-2xl p-6 md:p-8 shadow-sm lg:col-span-2 space-y-6">
          <div className="flex items-center justify-between border-b border-gray-100 pb-5">
            <div>
              <span className="text-xs text-gray-400 font-bold uppercase tracking-wider block mb-1">{t('interviews.job', 'Position')}</span>
              <h3 className="font-bold text-gray-800 text-xl">{interview.jobTitle}</h3>
              <p className="text-sm font-semibold text-gray-500 mt-0.5">{interview.companyName}</p>
            </div>
            <StatusBadge status={interview.status} />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="p-4 rounded-xl bg-blue-50/50 border border-blue-100 space-y-1">
              <span className="text-[10px] font-bold text-blue-600 uppercase tracking-wider">{t('interviews.dateTime', 'Date & Time')}</span>
              <p className="text-sm font-bold text-gray-800">{formatDate(interview.interviewDate)}</p>
              <p className="text-xs font-semibold text-blue-700">{formatTime(interview.interviewDate)}</p>
            </div>

            <div className="p-4 rounded-xl bg-purple-50/50 border border-purple-100 space-y-1">
              <span className="text-[10px] font-bold text-purple-600 uppercase tracking-wider">{t('interviews.interviewType', 'Mode')}</span>
              <p className="text-sm font-bold text-gray-800 flex items-center gap-1.5 mt-0.5">
                {isOnline ? <Video className="w-4 h-4 text-blue-600" /> : <MapPin className="w-4 h-4 text-purple-600" />}
                {isOnline ? t('interviews.typeVideo', 'Online Video Call') : t('interviews.typeOnsite', 'Onsite Office Meeting')}
              </p>
            </div>
          </div>

          {/* Meeting location / link */}
          <div className="space-y-2 pt-2">
            <h4 className="text-xs font-bold text-gray-400 uppercase tracking-wider">
              {isOnline ? t('interviews.meetingLink', 'Meeting Link') : t('interviews.location', 'Location')}
            </h4>
            <div className="p-4 bg-gray-50 rounded-xl border border-gray-100 text-sm font-semibold text-gray-700">
              {isOnline && interview.meetingLink ? (
                <a href={interview.meetingLink} target="_blank" rel="noopener noreferrer" className="text-blue-600 hover:underline break-all">
                  {interview.meetingLink}
                </a>
              ) : (
                interview.meetingLocation || t('companies.noLocation', 'No location specified')
              )}
            </div>
          </div>

          {/* Notes */}
          {interview.notes && (
            <div className="space-y-2 pt-2">
              <h4 className="text-xs font-bold text-gray-400 uppercase tracking-wider">{t('interviews.notes', 'Internal Notes')}</h4>
              <p className="text-sm text-gray-600 leading-relaxed whitespace-pre-line bg-gray-50/50 p-4 rounded-xl border border-gray-100">
                {interview.notes}
              </p>
            </div>
          )}
        </div>

        {/* Interviewer & Candidate Sidebar */}
        <div className="bg-white border border-gray-200 rounded-2xl p-6 shadow-sm space-y-6">
          <div>
            <h3 className="font-bold text-gray-800 text-sm border-b border-gray-100 pb-3 uppercase tracking-wider mb-4">
              {t('interviews.interviewer', 'Interviewer Info')}
            </h3>
            <div className="space-y-3 text-sm font-medium text-gray-600">
              <div className="flex items-center gap-3">
                <User className="w-4 h-4 text-gray-400 flex-shrink-0" />
                <span className="font-bold text-gray-800">{interview.interviewerName}</span>
              </div>
              <div className="flex items-center gap-3">
                <Mail className="w-4 h-4 text-gray-400 flex-shrink-0" />
                <span className="text-xs">{interview.interviewerEmail}</span>
              </div>
            </div>
          </div>

          <div>
            <h3 className="font-bold text-gray-800 text-sm border-b border-gray-100 pb-3 uppercase tracking-wider mb-4">
              {t('interviews.candidate', 'Candidate Info')}
            </h3>
            <div className="space-y-3 text-sm font-medium text-gray-600">
              <div className="flex items-center gap-3">
                <User className="w-4 h-4 text-gray-400 flex-shrink-0" />
                <Link to={`/candidates/${interview.candidateId}`} className="font-bold text-blue-600 hover:underline">
                  {interview.candidateName}
                </Link>
              </div>
            </div>
          </div>
        </div>
      </div>

      <InterviewStatusModal
        open={statusModalOpen}
        currentStatus={interview.status}
        onConfirm={handleUpdateStatusConfirm}
        onCancel={() => setStatusModalOpen(false)}
        loading={statusLoading}
      />
    </div>
  );
};

export default InterviewDetail;
