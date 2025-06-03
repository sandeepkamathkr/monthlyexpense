import React, { useState, ReactNode } from 'react';

interface CollapsibleCardProps {
  title: string;
  children: ReactNode;
  defaultExpanded?: boolean;
}

export const CollapsibleCard: React.FC<CollapsibleCardProps> = ({ 
  title, 
  children, 
  defaultExpanded = true 
}) => {
  const [isExpanded, setIsExpanded] = useState<boolean>(defaultExpanded);

  const toggleExpand = (): void => {
    setIsExpanded(!isExpanded);
  };

  return (
    <div className="card mb-4">
      <div className="card-header d-flex justify-content-between align-items-center">
        <span>{title}</span>
        <button 
          className="btn btn-sm btn-link p-0" 
          onClick={toggleExpand}
          aria-expanded={isExpanded}
          aria-controls="collapsible-content"
        >
          <i className={`bi ${isExpanded ? 'bi-chevron-up' : 'bi-chevron-down'}`}></i>
        </button>
      </div>
      {isExpanded && (
        <div className="card-body" id="collapsible-content">
          {children}
        </div>
      )}
    </div>
  );
};