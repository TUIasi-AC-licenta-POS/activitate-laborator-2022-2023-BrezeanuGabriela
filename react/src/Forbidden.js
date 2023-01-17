import React from 'react';
import Home from "./Home";
import './index.css';

class Forbidden extends React.Component {
    constructor(props) {
        super(props);
    }

    render() {
        return (
            <div>
                <h3>403 FORBIDDEN</h3>
            </div>
        );
        }
}

// trebuie exportata fiecare componenta pe care o folosesc intr-un fisier
export default Forbidden;