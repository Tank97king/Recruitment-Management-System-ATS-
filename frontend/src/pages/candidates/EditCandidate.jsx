import React, { useState, useEffect } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { toast } from 'react-hot-toast';
import { ArrowLeft, Loader2 } from 'lucide-react';
import axiosClient from '../../services/axiosClient';
import CandidateForm from './components/CandidateForm';

const EditCandidate = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { t } = useTranslation();
  const [loading, setLoading] = useState(true);
  const [submitLoading, setSubmitLoading] = useState(false);
  const [candidate, setCandidate] = useState(null);

  useEffect(() => {
    const fetchCandidate = async () => {
      try {
        const response = await axiosClient.get(`/candidates/${id}`);
        setCandidate(response.data.data);
      } catch (err) {
        console.error('Error loading candidate profile:', err);
        toast.error(t('common.error', 'Failed to load candidate profile.'));
        navigate('/candidates');
      } finally {
        setLoading(false);
      }
    };
    fetchCandidate();
  }, [id, navigate, t]);

  const onSubmit = async (data) => {
    setSubmitLoading(true);
    try {
      await axiosClient.put(`/candidates/${id}`, data);
      toast.success(t('common.success', 'Candidate profile updated successfully!'));
      navigate('/candidates');
    } catch (err) {
      console.error('Error updating candidate profile:', err);
      const msg = err.response?.data?.message || t('common.error', 'Failed to update candidate profile.');
      toast.error(msg);
    } finally {
      setSubmitLoading(false);
    }
  };

  return (
    <div className="max-w-3xl mx-auto space-y-6 animate-fadeIn">
      <div className="flex items-center gap-4">
        <Link 
          to="/candidates" 
          className="p-2 border border-gray-200 text-gray-500 hover:text-gray-800 rounded-xl hover:bg-gray-50 transition-colors bg-white shadow-sm"
        >
          <ArrowLeft className="w-4 h-4" />
        </Link>
        <div>
          <h2 className="text-2xl font-bold text-gray-800 tracking-tight">{t('candidates.editCandidate', 'Edit Candidate')}</h2>
          <p className="text-xs text-gray-400 font-semibold uppercase tracking-wider">{t('candidates.subtitle', 'Modify candidate profile details')}</p>
        </div>
      </div>

      <div className="bg-white border border-gray-200 rounded-2xl p-6 md:p-8 shadow-sm">
        {loading ? (
          <div className="flex flex-col items-center justify-center py-12 gap-3 text-gray-400 font-semibold text-sm">
            <Loader2 className="w-8 h-8 animate-spin text-blue-500" />
            <span>{t('common.loading', 'Loading candidate profile...')}</span>
          </div>
        ) : (
          <CandidateForm 
            defaultValues={candidate} 
            onSubmit={onSubmit} 
            loading={submitLoading} 
            submitText={t('candidates.editCandidate', 'Update Profile')} 
          />
        )}
      </div>
    </div>
  );
};

export default EditCandidate;
