import React, { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { toast } from 'react-hot-toast';
import { ArrowLeft, Edit2, Mail, Phone, Calendar, MapPin, Briefcase, GraduationCap, Clock, Loader2, FileText } from 'lucide-react';
import axiosClient from '../../services/axiosClient';
import SkillBadge from '../../components/ui/SkillBadge';
import CvUpload from './components/CvUpload';
import CvInfoCard from './components/CvInfoCard';

const CandidateDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { t } = useTranslation();
  const [loading, setLoading] = useState(true);
  const [candidate, setCandidate] = useState(null);
  const [isReplacingCv, setIsReplacingCv] = useState(false);

  const fetchCandidate = async () => {
    try {
      const response = await axiosClient.get(`/candidates/${id}`);
      setCandidate(response.data.data);
    } catch (err) {
      console.error('Error fetching candidate details:', err);
      toast.error(t('common.error', 'Failed to load candidate details.'));
      navigate('/candidates');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCandidate();
  }, [id]);

  const handleCvUploadSuccess = (updatedCandidate) => {
    setCandidate(updatedCandidate);
    setIsReplacingCv(false);
  };

  const handleCvDeleteSuccess = () => {
    setCandidate(prev => ({
      ...prev,
      hasCv: false,
      cvFileName: null,
      uploadedAt: null,
    }));
  };

  const formatDate = (isoString) => {
    if (!isoString) return 'N/A';
    return new Date(isoString).toLocaleDateString();
  };

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] gap-3 text-gray-450 font-semibold text-sm">
        <Loader2 className="w-8 h-8 animate-spin text-blue-500" />
        <span>{t('common.loading', 'Loading candidate details...')}</span>
      </div>
    );
  }

  if (!candidate) return null;

  return (
    <div className="max-w-4xl mx-auto space-y-6 animate-fadeIn">
      {/* Header controls */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex items-center gap-4">
          <Link 
            to="/candidates" 
            className="p-2 border border-gray-200 text-gray-500 hover:text-gray-800 rounded-xl hover:bg-gray-50 transition-colors bg-white shadow-sm"
          >
            <ArrowLeft className="w-4 h-4" />
          </Link>
          <div>
            <h2 className="text-2xl font-bold text-gray-800 tracking-tight">{candidate.fullName}</h2>
            <p className="text-xs text-gray-400 font-semibold uppercase tracking-wider">{t('candidates.candidateDetails', 'Candidate Profile Details')}</p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <Link
            to={`/candidates/${id}/edit`}
            className="inline-flex items-center px-4 py-2.5 bg-blue-600 text-white font-semibold text-sm rounded-xl hover:bg-blue-700 transition-colors shadow-md shadow-blue-100 gap-1.5"
          >
            <Edit2 className="w-4 h-4" /> {t('candidates.editCandidate', 'Edit Profile')}
          </Link>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 items-start">
        {/* Profile Card & Resume */}
        <div className="lg:col-span-2 space-y-6">
          {/* Main Info Block */}
          <div className="bg-white border border-gray-200 rounded-2xl p-6 md:p-8 shadow-sm space-y-6">
            <div className="flex items-center gap-4 border-b border-gray-100 pb-5">
              <div className="w-16 h-16 rounded-2xl bg-blue-50 text-blue-600 flex items-center justify-center font-bold text-2xl border border-blue-100 uppercase">
                {candidate.fullName.charAt(0)}
              </div>
              <div>
                <h3 className="font-bold text-gray-800 text-xl">{candidate.fullName}</h3>
                {candidate.currentTitle && (
                  <p className="text-sm font-semibold text-gray-500">{candidate.currentTitle}</p>
                )}
              </div>
            </div>

            <div className="space-y-3">
              <h4 className="text-sm font-bold text-gray-400 uppercase tracking-wider">{t('jobs.description', 'Professional Summary')}</h4>
              <p className="text-sm text-gray-600 leading-relaxed font-medium whitespace-pre-line">
                {candidate.summary || t('common.noData', 'No summary provided.')}
              </p>
            </div>

            <div className="space-y-3 pt-4 border-t border-gray-100">
              <h4 className="text-sm font-bold text-gray-400 uppercase tracking-wider">{t('candidates.skills', 'Skills')}</h4>
              <div className="flex flex-wrap gap-1.5">
                {candidate.skills && candidate.skills.length > 0 ? (
                  candidate.skills.map((skill, idx) => (
                    <SkillBadge key={idx} skill={skill} />
                  ))
                ) : (
                  <span className="text-sm text-gray-400 font-semibold">{t('common.noData', 'No skills listed')}</span>
                )}
              </div>
            </div>
          </div>

          {/* CV Attachment Box */}
          <div className="bg-white border border-gray-200 rounded-2xl p-6 shadow-sm space-y-4">
            <h3 className="font-bold text-gray-800 text-base flex items-center gap-2 border-b border-gray-100 pb-3">
              <FileText className="w-5 h-5 text-blue-600" />
              {t('candidates.cvDocument', 'CV Document Attachment')}
            </h3>

            {candidate.hasCv && !isReplacingCv ? (
              <CvInfoCard
                candidate={candidate}
                onReplace={() => setIsReplacingCv(true)}
                onDeleteSuccess={handleCvDeleteSuccess}
              />
            ) : (
              <div className="space-y-3">
                {isReplacingCv && (
                  <div className="flex justify-between items-center mb-2">
                    <span className="text-xs font-bold text-gray-400 uppercase">{t('candidates.uploadCv', 'Upload replacement file')}</span>
                    <button
                      onClick={() => setIsReplacingCv(false)}
                      className="text-xs text-gray-500 hover:text-gray-700 font-bold underline"
                    >
                      {t('common.cancel', 'Cancel')}
                    </button>
                  </div>
                )}
                <CvUpload candidateId={candidate.id} onUploadSuccess={handleCvUploadSuccess} />
              </div>
            )}
          </div>
        </div>

        {/* Sidebar Info */}
        <div className="bg-white border border-gray-200 rounded-2xl p-6 shadow-sm space-y-6">
          <h3 className="font-bold text-gray-800 text-sm border-b border-gray-100 pb-3 uppercase tracking-wider">
            {t('candidates.candidateDetails', 'Contact & Education')}
          </h3>

          <div className="space-y-4 text-sm font-medium text-gray-600">
            <div className="flex items-center gap-3">
              <Mail className="w-4 h-4 text-gray-400 flex-shrink-0" />
              <span>{candidate.email}</span>
            </div>
            <div className="flex items-center gap-3">
              <Phone className="w-4 h-4 text-gray-400 flex-shrink-0" />
              <span>{candidate.phone || 'N/A'}</span>
            </div>
            <div className="flex items-center gap-3">
              <MapPin className="w-4 h-4 text-gray-400 flex-shrink-0" />
              <span>{candidate.address || t('companies.noLocation', 'No location provided')}</span>
            </div>
            <div className="flex items-center gap-3">
              <Briefcase className="w-4 h-4 text-gray-400 flex-shrink-0" />
              <span>{t('candidates.experienceYears', 'Experience')}: <strong className="text-gray-800 font-semibold">{candidate.yearsOfExperience !== undefined ? `${candidate.yearsOfExperience} yrs` : 'N/A'}</strong></span>
            </div>
            <div className="flex items-center gap-3">
              <GraduationCap className="w-4 h-4 text-gray-400 flex-shrink-0" />
              <span>{candidate.highestEducation || 'N/A'}</span>
            </div>
            <div className="flex items-center gap-3 pt-2 border-t border-gray-100 text-xs text-gray-400 font-semibold">
              <Clock className="w-4 h-4 text-gray-400 flex-shrink-0" />
              <span>{t('applications.appliedDate', 'Registered')}: {formatDate(candidate.createdAt)}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CandidateDetail;
