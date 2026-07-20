import React from 'react';
import { useTranslation } from 'react-i18next';
import { Mail, Calendar } from 'lucide-react';

const AvatarCard = ({ user = {} }) => {
  const { t } = useTranslation();

  const getInitials = (name) => {
    if (!name) return 'U';
    return name.charAt(0).toUpperCase();
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString();
  };

  return (
    <div className="bg-white border border-gray-200 rounded-2xl p-6 shadow-sm flex flex-col items-center text-center space-y-4">
      {/* Avatar Circle */}
      <div className="w-20 h-20 rounded-2xl bg-blue-50 text-blue-700 flex items-center justify-center font-extrabold text-3xl border border-blue-100 uppercase shadow-inner">
        {getInitials(user.fullName)}
      </div>

      {/* User Info labels */}
      <div className="space-y-1">
        <h3 className="text-lg font-black text-gray-800 tracking-tight">{user.fullName || 'Recruiter'}</h3>
        <span className="inline-flex px-2.5 py-0.5 rounded-full text-[10px] font-black uppercase tracking-wider bg-blue-50 text-blue-700 border border-blue-100">
          {user.role || 'RECRUITER'}
        </span>
      </div>

      {/* Profile Details List */}
      <div className="w-full pt-4 border-t border-gray-100 space-y-2 text-xs font-semibold text-gray-450 text-left">
        <div className="flex items-center gap-2 truncate">
          <Mail className="w-4 h-4 text-gray-400 flex-shrink-0" />
          <span className="truncate">{user.email || 'user@example.com'}</span>
        </div>
        <div className="flex items-center gap-2">
          <Calendar className="w-4 h-4 text-gray-400 flex-shrink-0" />
          <span>{t('applications.appliedDate', 'Joined')}: {formatDate(user.createdAt)}</span>
        </div>
      </div>
    </div>
  );
};

export default AvatarCard;
