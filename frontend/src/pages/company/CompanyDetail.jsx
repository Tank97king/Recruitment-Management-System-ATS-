import React, { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { toast } from 'react-hot-toast';
import { ArrowLeft, Edit2, Globe, Mail, Phone, MapPin, Calendar, Clock, Loader2 } from 'lucide-react';
import axiosClient from '../../services/axiosClient';

const CompanyDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { t } = useTranslation();
  const [loading, setLoading] = useState(true);
  const [company, setCompany] = useState(null);

  useEffect(() => {
    const fetchCompany = async () => {
      try {
        const response = await axiosClient.get(`/companies/${id}`);
        setCompany(response.data.data);
      } catch (err) {
        console.error('Error fetching company:', err);
        toast.error(t('common.error', 'Failed to load company profile details.'));
        navigate('/companies');
      } finally {
        setLoading(false);
      }
    };
    fetchCompany();
  }, [id, navigate, t]);

  const formatDate = (isoString) => {
    if (!isoString) return 'N/A';
    return new Date(isoString).toLocaleDateString();
  };

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] gap-3 text-gray-450 font-semibold text-sm">
        <Loader2 className="w-8 h-8 animate-spin text-blue-500" />
        <span>{t('common.loading', 'Loading company details...')}</span>
      </div>
    );
  }

  if (!company) return null;

  return (
    <div className="max-w-4xl mx-auto space-y-6 animate-fadeIn">
      {/* Header controls */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex items-center gap-4">
          <Link 
            to="/companies" 
            className="p-2 border border-gray-200 text-gray-500 hover:text-gray-800 rounded-xl hover:bg-gray-50 transition-colors bg-white shadow-sm"
          >
            <ArrowLeft className="w-4 h-4" />
          </Link>
          <div>
            <h2 className="text-2xl font-bold text-gray-800 tracking-tight">{company.companyName}</h2>
            <p className="text-xs text-gray-400 font-semibold uppercase tracking-wider">{t('companies.companyDetails', 'Company Profile Details')}</p>
          </div>
        </div>
        <Link
          to={`/companies/${id}/edit`}
          className="inline-flex items-center px-4 py-2.5 bg-blue-600 text-white font-semibold text-sm rounded-xl hover:bg-blue-700 transition-colors shadow-md shadow-blue-100 gap-1.5 self-start sm:self-auto"
        >
          <Edit2 className="w-4 h-4" /> {t('companies.editCompany', 'Edit Profile')}
        </Link>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 items-start">
        {/* Main Details Panel */}
        <div className="bg-white border border-gray-200 rounded-2xl p-6 md:p-8 shadow-sm lg:col-span-2 space-y-6">
          <div className="flex items-center gap-4 border-b border-gray-100 pb-4">
            <div className="w-14 h-14 rounded-2xl bg-blue-50 text-blue-600 flex items-center justify-center font-bold text-2xl border border-blue-100">
              {company.companyName.charAt(0).toUpperCase()}
            </div>
            <div>
              <h3 className="font-bold text-gray-800 text-lg">{company.companyName}</h3>
              <span className="text-xs font-semibold text-gray-400">ID: {company.id}</span>
            </div>
          </div>

          <div className="space-y-3">
            <h4 className="text-sm font-bold text-gray-400 uppercase tracking-wider">{t('companies.description', 'About Company')}</h4>
            <p className="text-sm text-gray-600 leading-relaxed font-semibold">
              {company.description || t('common.noData', 'No description provided for this company profile.')}
            </p>
          </div>
        </div>

        {/* Contact/Metadata Sidebar */}
        <div className="bg-white border border-gray-200 rounded-2xl p-6 shadow-sm space-y-6">
          <h3 className="font-bold text-gray-800 text-sm border-b border-gray-100 pb-3 uppercase tracking-wider">
            {t('companies.location', 'Contact & Location')}
          </h3>

          <div className="space-y-4 text-sm font-medium text-gray-600">
            <div className="flex items-center gap-3">
              <Mail className="w-4 h-4 text-gray-400 flex-shrink-0" />
              <span>{company.email || 'N/A'}</span>
            </div>
            <div className="flex items-center gap-3">
              <Phone className="w-4 h-4 text-gray-400 flex-shrink-0" />
              <span>{company.phone || 'N/A'}</span>
            </div>
            <div className="flex items-center gap-3">
              <Globe className="w-4 h-4 text-gray-400 flex-shrink-0" />
              {company.website ? (
                <a href={company.website} target="_blank" rel="noopener noreferrer" className="text-blue-600 hover:underline">
                  {company.website}
                </a>
              ) : (
                <span>{t('companies.noWebsite', 'No website provided')}</span>
              )}
            </div>
            <div className="flex items-center gap-3">
              <MapPin className="w-4 h-4 text-gray-400 flex-shrink-0" />
              <span>{company.address || t('companies.noLocation', 'No location provided')}</span>
            </div>
            <div className="flex items-center gap-3 pt-2 border-t border-gray-100 text-xs text-gray-400 font-semibold">
              <Calendar className="w-4 h-4 text-gray-400 flex-shrink-0" />
              <span>{t('applications.appliedDate', 'Created')}: {formatDate(company.createdAt)}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CompanyDetail;
