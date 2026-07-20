import React from 'react';

const DataTable = ({ columns, data = [], loading, emptyMessage = 'No records found' }) => {
  if (loading) {
    return (
      <div className="w-full overflow-hidden border border-gray-200 rounded-2xl bg-white animate-pulse">
        <div className="h-12 bg-gray-50 border-b border-gray-100 flex items-center px-6">
          <div className="h-4 bg-gray-200 rounded-md w-1/4"></div>
        </div>
        <div className="divide-y divide-gray-100">
          {Array.from({ length: 5 }).map((_, i) => (
            <div key={i} className="h-16 flex items-center px-6 gap-6">
              <div className="h-4 bg-gray-200 rounded-md w-1/3"></div>
              <div className="h-4 bg-gray-200 rounded-md w-1/4"></div>
              <div className="h-4 bg-gray-200 rounded-md w-1/12"></div>
              <div className="h-4 bg-gray-200 rounded-md w-1/6"></div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="w-full border border-gray-200 rounded-2xl bg-white shadow-sm overflow-hidden">
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-100 text-left">
          <thead className="bg-gray-50 text-xs font-bold text-gray-400 uppercase tracking-wider">
            <tr>
              {columns.map((col, idx) => (
                <th 
                  key={idx} 
                  className={`px-6 py-4 ${col.className || ''}`}
                  style={col.style}
                >
                  {col.header}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100 text-sm">
            {data && data.length > 0 ? (
              data.map((row, rowIdx) => (
                <tr key={rowIdx} className="hover:bg-gray-50/50 transition-colors">
                  {columns.map((col, colIdx) => (
                    <td key={colIdx} className={`px-6 py-4 ${col.cellClassName || ''}`}>
                      {col.cell ? col.cell(row) : row[col.accessor]}
                    </td>
                  ))}
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan={columns.length} className="px-6 py-12 text-center text-gray-400 font-semibold">
                  {emptyMessage}
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default DataTable;
