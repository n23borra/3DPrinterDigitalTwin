import React from 'react';
import clsx from 'clsx';

/**
 * Renders a styled primary button used throughout the application.
 * @param {object} props - Button configuration props forwarded to the native element.
 * @param {React.ReactNode} props.children - Content displayed inside the button.
 * @param {string} [props.className] - Optional extra CSS classes to append to the base styling.
 * @returns {JSX.Element} Interactive button element with consistent styling.
 */
export default function Button({children, className, ...props}) {
    return (
        <button
            className={clsx(
                'px-4 py-2 rounded text-white bg-blue-600 hover:bg-blue-700 disabled:opacity-50',
                className
            )}
            {...props}
        >
            {children}
        </button>
    );
}