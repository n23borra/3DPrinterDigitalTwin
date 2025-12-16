import React from 'react';

/**
 * Provides an accessible modal overlay with a close button and custom content area.
 * @param {object} props - Modal configuration props.
 * @param {boolean} props.open - Whether the modal should be rendered.
 * @param {() => void} props.onClose - Callback invoked when the close button is clicked.
 * @param {React.ReactNode} props.children - Content rendered inside the modal container.
 * @param {string} [props.className] - Optional classes applied to the modal content wrapper.
 * @returns {JSX.Element|null} Modal markup when open, otherwise null.
 */
export default function Modal({open, onClose, children, className = ''}) {
    if (!open) return null;
    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className={`bg-white rounded shadow relative p-4 max-h-full overflow-y-auto ${className}`}>
                <button type="button" onClick={onClose} className="absolute top-2 right-2 text-gray-600 text-2xl">
                    &times;
                </button>
                {children}
            </div>
        </div>
    );
}