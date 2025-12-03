import React from 'react';

/**
 * Displays a centered spinner used while asynchronous data is loading.
 * @returns {JSX.Element} A loader animation wrapped in spacing utilities.
 */
export default function Loader() {
    return (
        <div className="flex items-center justify-center py-10">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-white" />
        </div>
    );
}