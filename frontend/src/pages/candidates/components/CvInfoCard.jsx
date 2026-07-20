import React, { useState } from 'react';
import { FileText, Download, Trash2, RefreshCw, Calendar, Loader2 } from 'lucide-react';
import { toast } from 'react-hot-toast';
import axiosClient from '../../../services/axiosClient';
import ConfirmDialog from '../../../components/ui/ConfirmDialog';

const CvInfoCard = ({ candidateId, cvFileName, uploadedAt, onReplace, onDeleteSuccess }) => {
  const [downloading, setDownloading] = useState(false);
  const [deleteOpen, setDeleteOpen] = useState(false);
  const [deleteLoading, setDeleteLoading] = useState(false);

  const handleDownload = async () => {
    setDownloading(true);
    try {
      const response = await axiosClient.get(`/candidates/${candidateId}/cv`, {
        responseType: 'blob',
      });
      
      const blob = new Blob([response.data], { type: 'application/pdf' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', cvFileName || 'resume.pdf');
      document.body.appendChild(link);
      link.click();
      link.parentNode.removeChild(link);
      window.URL.revokeObjectURL(url);
    } catch (err) {
      console.error('Error downloading CV:', err);
      toast.error('Failed to download CV file.');
    } finally {
      setDownloading(false);
    }
  };

  const handleDeleteConfirm = async () => {
    setDeleteLoading(true);
    try {
      await axiosClient.delete(`/candidates/${candidateId}/cv`);
      toast.success('CV deleted successfully!');
      setDeleteOpen(false);
      onDeleteSuccess();
    } catch (err) {
      console.error('Error deleting CV:', err);
      toast.error('Failed to delete CV.');
    } finally {
      setDeleteLoading(false);
    }
  };

  const formatDate = (isoString) => {
    if (!isoString) return 'N/A';
    return new Date(isoString).toLocaleString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className="bg-white border border-gray-200 rounded-2xl p-5 shadow-sm space-y-4">
      <div className="flex items-center gap-3">
        <div className="p-3 bg-red-50 text-red-600 border border-red-100 rounded-xl">
          <FileText className="w-6 h-6" />
        </div>
        <div className="flex-1 min-w-0">
          <p className="text-sm font-bold text-gray-800 truncate" title={cvFileName}>
            {cvFileName}
          </p>
          <div className="flex items-center gap-1.5 text-xs text-gray-400 font-semibold mt-1">
            <Calendar className="w-3.5 h-3.5" />
            <span>Uploaded: {formatDate(uploadedAt)}</span>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-3 gap-2 border-t border-gray-100 pt-4">
        {/* Download */}
        <button
          onClick={handleDownload}
          disabled={downloading}
          className="flex flex-col items-center justify-center py-2.5 px-2 border border-gray-200 hover:border-gray-300 text-gray-600 hover:text-gray-800 font-bold text-[10px] rounded-xl hover:bg-gray-50 transition-colors uppercase tracking-wider gap-1 bg-white shadow-sm disabled:opacity-50"
          title="Download PDF CV"
        >
          {downloading ? (
            <Loader2 className="w-4 h-4 animate-spin text-blue-500" />
          ) : (
            <Download className="w-4 h-4 text-blue-500" />
          )}
          <span>Download</span>
        </button>

        {/* Replace */}
        <button
          onClick={onReplace}
          disabled={downloading}
          className="flex flex-col items-center justify-center py-2.5 px-2 border border-gray-200 hover:border-gray-300 text-gray-600 hover:text-gray-800 font-bold text-[10px] rounded-xl hover:bg-gray-50 transition-colors uppercase tracking-wider gap-1 bg-white shadow-sm disabled:opacity-50"
          title="Upload replacement"
        >
          <RefreshCw className="w-4 h-4 text-amber-500" />
          <span>Replace</span>
        </button>

        {/* Delete */}
        <button
          onClick={() => setDeleteOpen(true)}
          disabled={downloading}
          className="flex flex-col items-center justify-center py-2.5 px-2 border border-gray-200 hover:border-gray-300 text-gray-600 hover:text-gray-800 font-bold text-[10px] rounded-xl hover:bg-red-50 hover:border-red-100 transition-colors uppercase tracking-wider gap-1 bg-white shadow-sm disabled:opacity-50"
          title="Delete CV file"
        >
          <Trash2 className="w-4 h-4 text-red-500" />
          <span>Delete</span>
        </button>
      </div>

      <ConfirmDialog
        open={deleteOpen}
        title="Delete Candidate CV"
        message="Are you sure you want to delete this candidate's CV file? This action is permanent and cannot be undone."
        onConfirm={handleDeleteConfirm}
        onCancel={() => setDeleteOpen(false)}
        confirmText="Delete"
        loading={deleteLoading}
      />
    </div>
  );
};

export default CvInfoCard;
