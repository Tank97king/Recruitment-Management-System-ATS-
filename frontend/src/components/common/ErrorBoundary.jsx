import React, { Component } from 'react';
import { AlertTriangle, RotateCcw, ChevronDown, ChevronUp } from 'lucide-react';

class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null, errorInfo: null, showStack: false };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    console.error('ErrorBoundary caught an error:', error, errorInfo);
    this.setState({ errorInfo });
  }

  handleReset = () => {
    this.setState({ hasError: false, error: null, errorInfo: null, showStack: false });
    window.location.reload();
  };

  render() {
    if (this.state.hasError) {
      const { error, errorInfo, showStack } = this.state;
      return (
        <div className="min-h-[60vh] flex flex-col items-center justify-center p-8 text-center animate-fadeIn">
          <div className="bg-red-50 border border-red-200 rounded-2xl p-8 max-w-2xl w-full space-y-5 shadow-sm text-left">
            {/* Header */}
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-red-100 text-red-600 flex items-center justify-center border border-red-200 flex-shrink-0">
                <AlertTriangle className="w-5 h-5" />
              </div>
              <div>
                <h3 className="text-base font-bold text-red-800">Something went wrong</h3>
                <p className="text-xs text-red-500 font-medium">A runtime error occurred in this component</p>
              </div>
            </div>

            {/* Error Message */}
            <div className="bg-white border border-red-200 rounded-xl p-4 font-mono text-sm text-red-700 break-all leading-relaxed">
              <span className="font-bold text-red-500 text-xs uppercase tracking-wider block mb-1">Error:</span>
              {error?.toString()}
            </div>

            {/* Component Stack Toggle */}
            {errorInfo?.componentStack && (
              <div>
                <button
                  onClick={() => this.setState(s => ({ showStack: !s.showStack }))}
                  className="flex items-center gap-1.5 text-xs font-bold text-red-600 hover:text-red-700 transition-colors"
                >
                  {showStack ? <ChevronUp className="w-3.5 h-3.5" /> : <ChevronDown className="w-3.5 h-3.5" />}
                  {showStack ? 'Hide' : 'Show'} component stack
                </button>
                {showStack && (
                  <pre className="mt-2 bg-white border border-red-100 rounded-xl p-4 font-mono text-xs text-gray-600 overflow-auto max-h-48 whitespace-pre-wrap break-all leading-relaxed">
                    {errorInfo.componentStack}
                  </pre>
                )}
              </div>
            )}

            {/* Reload Button */}
            <button
              onClick={this.handleReset}
              className="inline-flex items-center px-4 py-2.5 bg-blue-600 hover:bg-blue-700 text-white font-semibold text-sm rounded-xl transition-colors shadow-md shadow-blue-100 gap-1.5"
            >
              <RotateCcw className="w-4 h-4" /> Reload Page
            </button>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;

