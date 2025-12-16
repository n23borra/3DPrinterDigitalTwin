import React, { useState } from 'react';

/**
 * Collects a list of tags (emails) while preventing duplicates and allowing removal.
 * @param {object} props - Tag input configuration.
 * @param {string[]} props.value - Current list of tags displayed as pills.
 * @param {(next: string[]) => void} props.onChange - Callback receiving the updated tag list.
 * @returns {JSX.Element} Interactive input with removable tag chips.
 */
export default function TagInput({ value = [], onChange }) {
    const [input, setInput] = useState('');

    /**
     * Normalizes and adds a tag to the list if it is non-empty and not already present.
     * @param {string} tag - Raw tag value provided by the user.
     * @returns {void}
     */
    const addTag = (tag) => {
        const email = tag.trim();
        if (!email) return;
        if (!value.includes(email)) {
            onChange([...value, email]);
        }
    };

    /**
     * Commits the current input value as a new tag and resets the input field.
     * @returns {void}
     */
    const commitInput = () => {
        if (input.trim()) {
            addTag(input);
            setInput('');
        }
    };

    /**
     * Handles keyboard shortcuts to convert the current input into a tag.
     * @param {React.KeyboardEvent<HTMLInputElement>} e - Keyboard event triggered in the textbox.
     * @returns {void}
     */
    const handleKeyDown = (e) => {
        if (['Enter', ',', ';'].includes(e.key)) {
            e.preventDefault();
            commitInput();
        }
    };

    /**
     * Removes a tag by its index from the current list.
     * @param {number} idx - Position of the tag to remove.
     * @returns {void}
     */
    const removeTag = (idx) => {
        const newValue = value.filter((_, i) => i !== idx);
        onChange(newValue);
    };

    return (
        <div className="w-full flex flex-wrap gap-1 border rounded-md p-2 focus-within:ring-2 focus-within:ring-blue-500">
            {value.map((tag, idx) => (
                <span key={idx} className="bg-gray-200 rounded-full px-2 py-0.5 flex items-center">
                    <span className="mr-1">{tag}</span>
                    <button
                        type="button"
                        onClick={() => removeTag(idx)}
                        className="ml-1 text-gray-500 hover:text-red-600 focus:outline-none"
                    >
                        &times;
                    </button>
                </span>
            ))}
            <input
                type="text"
                value={input}
                onChange={(e) => setInput(e.target.value)}
                onKeyDown={handleKeyDown}
                onBlur={commitInput}
                className="flex-grow min-w-[120px] px-2 py-1 outline-none"
                placeholder="Type email and press Enter"
            />
        </div>
    );
}