import React from 'react';
import {fireEvent, render, screen, waitFor} from '@testing-library/react';
import {vi} from 'vitest';
import {Section} from './References';

describe('References Section editing', () => {
    const defaultProps = {
        title: 'Threats',
        data: [{id: 1, label: 'Threat 1', probability: 0.5}],
        form: {label: '', probability: ''},
        setForm: vi.fn(),
        onAdd: vi.fn(),
        onDelete: vi.fn(),
        onUpdate: vi.fn(),
        editable: true,
        valueKey: 'probability'
    };

    const renderSection = (overrideProps = {}) => {
        const props = {...defaultProps, ...overrideProps};
        const Wrapper = () => {
            const [formState, setFormState] = React.useState(props.form);
            return <Section {...props} form={formState} setForm={setFormState}/>;
        };
        return render(<Wrapper/>);
    };

    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('prevents updates when the numeric value is invalid', () => {
        const onUpdate = vi.fn().mockResolvedValue();
        renderSection({onUpdate});

        fireEvent.click(screen.getByRole('button', {name: /edit/i}));
        const valueInput = screen.getByTestId('edit-value-input');
        const saveButton = screen.getByTestId('save-edit-button');

        fireEvent.change(valueInput, {target: {value: 'abc'}});
        expect(screen.getByText(/please enter a valid number/i)).toBeInTheDocument();
        expect(saveButton).toBeDisabled();

        fireEvent.click(saveButton);
        expect(onUpdate).not.toHaveBeenCalled();
    });

    it('prevents updates when the value is out of range', () => {
        const onUpdate = vi.fn().mockResolvedValue();
        renderSection({onUpdate});

        fireEvent.click(screen.getByRole('button', {name: /edit/i}));
        const valueInput = screen.getByTestId('edit-value-input');
        const saveButton = screen.getByTestId('save-edit-button');

        fireEvent.change(valueInput, {target: {value: '1.2'}});
        expect(screen.getByText(/value must be between 0 and 1/i)).toBeInTheDocument();
        expect(saveButton).toBeDisabled();

        fireEvent.click(saveButton);
        expect(onUpdate).not.toHaveBeenCalled();
    });

    it('submits updates with a parsed numeric value when valid', async () => {
        const onUpdate = vi.fn().mockResolvedValue();
        renderSection({onUpdate});

        fireEvent.click(screen.getByRole('button', {name: /edit/i}));
        const valueInput = screen.getByTestId('edit-value-input');
        const saveButton = screen.getByTestId('save-edit-button');

        fireEvent.change(valueInput, {target: {value: '0.8'}});
        expect(saveButton).not.toBeDisabled();

        fireEvent.click(saveButton);

        await waitFor(() => {
            expect(onUpdate).toHaveBeenCalledWith(1, {label: 'Threat 1', probability: 3.2});
        });
    });

    it('disables adding new items when the create value is out of range', () => {
        const onAdd = vi.fn((event) => event.preventDefault());
        renderSection({onAdd});

        const labelInput = screen.getByPlaceholderText(/label/i);
        const valueInput = screen.getByPlaceholderText(/value/i);
        const addButton = screen.getByRole('button', {name: /add/i});

        fireEvent.change(labelInput, {target: {value: 'Threat 2'}});
        fireEvent.change(valueInput, {target: {value: '1.5'}});

        expect(screen.getByText(/value must be between 0 and 1/i)).toBeInTheDocument();
        expect(addButton).toBeDisabled();

        fireEvent.click(addButton);
        expect(onAdd).not.toHaveBeenCalled();
    });

    it('allows adding new items when the create value is within range', () => {
        const onAdd = vi.fn((event) => event.preventDefault());
        renderSection({onAdd});

        const labelInput = screen.getByPlaceholderText(/label/i);
        const valueInput = screen.getByPlaceholderText(/value/i);
        const addButton = screen.getByRole('button', {name: /add/i});

        fireEvent.change(labelInput, {target: {value: 'Threat 2'}});
        fireEvent.change(valueInput, {target: {value: '0.6'}});

        expect(screen.queryByText(/value must be between 0 and 1/i)).not.toBeInTheDocument();
        expect(addButton).not.toBeDisabled();

        fireEvent.submit(valueInput.closest('form'));
        expect(onAdd).toHaveBeenCalled();
    });
});