import React, { useState, useRef } from 'react';
import { Upload, FileText } from 'lucide-react';
import { toast } from 'react-hot-toast';
import axiosClient from '../../../services/axiosClient';

const CvUpload = ({ candidateId, onUploadSuccess }) => {
  const [dragActive, setDragActive] = useState(false);
  const [file, setFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [progress, setProgress] = useState(0);
  const fileInputRef = useRef(null);

  const handleDrag = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === "dragenter" || e.type === "dragover") {
      setDragActive(true);
    } else if (e.type === "dragleave") {
      setDragActive(false);
    }
  };

  const validateAndSetFile = (selectedFile) => {
    if (!selectedFile) return;

    if (selectedFile.type !== 'application/pdf' && !selectedFile.name.endsWith('.pdf')) {
      toast.error('Only PDF files are accepted.');
      return;
    }

    if (selectedFile.size > 5 * 1024 * 1024) {
      toast.error('Maximum file size is 5 MB.');
      return;
    }

    setFile(selectedFile);
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      validateAndSetFile(e.dataTransfer.files[0]);
    }
  };

  const handleFileChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      validateAndSetFile(e.target.files[0]);
    }
  };

  const onButtonClick = () => {
    fileInputRef.current.click();
  };

  const handleUpload = async () => {
    if (!file) return;

    setUploading(true);
    setProgress(0);

    const formData = new FormData();
    formData.append('file', file);

    try {
      const response = await axiosClient.post(`/candidates/${candidateId}/cv`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
        onUploadProgress: (progressEvent) => {
          const total = progressEvent.total || file.size;
          const current = progressEvent.loaded;
          const pct = Math.round((current / total) * 100);
          setProgress(pct);
        },
      });

      toast.success('CV uploaded successfully!');
      onUploadSuccess(response.data.data);
      setFile(null);
    } catch (err) {
      console.error('Error uploading CV:', err);
      const msg = err.response?.data?.message || 'Failed to upload CV file. Please try again.';
      toast.error(msg);
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="space-y-4">
      <div 
        onDragEnter={handleDrag}
        onDragOver={handleDrag}
        onDragLeave={handleDrag}
        onDrop={handleDrop}
        className={`border-2 border-dashed rounded-2xl p-6 text-center transition-all ${
          dragActive ? 'border-blue-500 bg-blue-50/50' : 'border-gray-300 hover:border-gray-400 bg-gray-50/30'
        }`}
      >
        <input
          ref={fileInputRef}
          type="file"
          accept=".pdf,application/pdf"
          onChange={handleFileChange}
          className="hidden"
          disabled={uploading}
        />

        <div className="flex flex-col items-center gap-3">
          <div className="p-3 rounded-full bg-blue-50 text-blue-600 border border-blue-100">
            <Upload className="w-6 h-6" />
          </div>
          <div>
            <p className="text-sm font-bold text-gray-700">
              Drag & drop candidate CV or{' '}
              <button 
                type="button" 
                onClick={onButtonClick}
                disabled={uploading}
                className="text-blue-600 hover:underline font-semibold"
              >
                browse
              </button>
            </p>
            <p className="text-xs text-gray-400 mt-1 font-semibold">Only PDF documents up to 5 MB</p>
          </div>
        </div>
      </div>

      {file && (
        <div className="bg-white border border-gray-200 rounded-2xl p-4 shadow-sm space-y-4">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-red-50 text-red-600 rounded-xl">
              <FileText className="w-5 h-5" />
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-xs font-bold text-gray-800 truncate">{file.name}</p>
              <p className="text-[10px] text-gray-400 font-bold uppercase mt-0.5">
                {(file.size / (1024 * 1024)).toFixed(2)} MB
              </p>
            </div>
            {!uploading && (
              <button 
                onClick={() => setFile(null)}
                className="text-xs text-red-600 hover:text-red-700 font-bold"
              >
                Clear
              </button>
            )}
          </div>

          {uploading ? (
            <div className="space-y-1.5">
              <div className="flex justify-between text-xs font-bold">
                <span className="text-gray-400 uppercase tracking-wider">Uploading...</span>
                <span className="text-blue-600">{progress}%</span>
              </div>
              <div className="w-full bg-gray-100 h-2 rounded-full overflow-hidden">
                <div 
                  className="h-full bg-blue-500 rounded-full transition-all duration-100" 
                  style={{ width: `${progress}%` }}
                />
              </div>
            </div>
          ) : (
            <button
              onClick={handleUpload}
              className="w-full flex justify-center py-2 px-4 bg-blue-600 hover:bg-blue-700 text-white font-bold text-xs rounded-xl shadow-sm transition-colors"
            >
              Upload Resume
            </button>
          )}
        </div>
      )}
    </div>
  );
};

export default CvUpload;
